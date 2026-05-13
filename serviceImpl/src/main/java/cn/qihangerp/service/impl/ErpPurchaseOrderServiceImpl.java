package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.utils.DateUtils;
import cn.qihangerp.model.bo.PurchaseOrderAddBo;
import cn.qihangerp.model.bo.PurchaseOrderAddItemBo;
import cn.qihangerp.model.bo.PurchaseOrderOptionBo;
import cn.qihangerp.model.entity.ErpPurchaseOrder;
import cn.qihangerp.model.entity.ErpPurchaseOrderItem;
import cn.qihangerp.model.entity.ErpPurchaseOrderShip;
import cn.qihangerp.model.query.SearchBo;
import cn.qihangerp.mapper.ErpPurchaseOrderItemMapper;
import cn.qihangerp.mapper.ErpPurchaseOrderMapper;
import cn.qihangerp.mapper.ErpPurchaseOrderShipMapper;
import cn.qihangerp.service.ErpPurchaseOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 1
* @description 针对表【erp_purchase_order(采购订单)】的数据库操作Service实现
* @createDate 2025-09-09 09:51:48
*/
@AllArgsConstructor
@Service
public class ErpPurchaseOrderServiceImpl extends ServiceImpl<ErpPurchaseOrderMapper, ErpPurchaseOrder>
    implements ErpPurchaseOrderService {
    private final ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    private final ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    private final ErpPurchaseOrderShipMapper shipMapper;

    private final String DATE_PATTERN =
            "^(?:(?:(?:\\d{4}-(?:0?[1-9]|1[0-2])-(?:0?[1-9]|1\\d|2[0-8]))|(?:(?:(?:\\d{2}(?:0[48]|[2468][048]|[13579][26])|(?:(?:0[48]|[2468][048]|[13579][26])00))-0?2-29))$)|(?:(?:(?:\\d{4}-(?:0?[13578]|1[02]))-(?:0?[1-9]|[12]\\d|30))$)|(?:(?:(?:\\d{4}-0?[13-9]|1[0-2])-(?:0?[1-9]|[1-2]\\d|30))$)|(?:(?:(?:\\d{2}(?:0[48]|[13579][26]|[2468][048])|(?:(?:0[48]|[13579][26]|[2468][048])00))-0?2-29))$)$";
    private final Pattern DATE_FORMAT = Pattern.compile(DATE_PATTERN);
    @Override
    public PageResult<ErpPurchaseOrder> queryPageList(SearchBo bo, PageQuery pageQuery) {
        if(org.springframework.util.StringUtils.hasText(bo.getStartTime())){
            Matcher matcher = DATE_FORMAT.matcher(bo.getStartTime());
            boolean b = matcher.find();
            if(b){
                bo.setStartTime(bo.getStartTime()+" 00:00:00");
            }
        }
        if(org.springframework.util.StringUtils.hasText(bo.getEndTime())){
            Matcher matcher = DATE_FORMAT.matcher(bo.getEndTime());
            boolean b = matcher.find();
            if(b){
                bo.setEndTime(bo.getEndTime()+" 23:59:59");
            }
        }

        LambdaQueryWrapper<ErpPurchaseOrder> queryWrapper = new LambdaQueryWrapper<ErpPurchaseOrder>()
                .eq(bo.getSupplierId()!=null, ErpPurchaseOrder::getSupplierId,bo.getSupplierId())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), ErpPurchaseOrder::getOrderNum,bo.getOrderNum())
                .eq(bo.getOrderStatus()!=null, ErpPurchaseOrder::getStatus,bo.getOrderStatus())
                .ge(org.springframework.util.StringUtils.hasText(bo.getStartTime()), ErpPurchaseOrder::getOrderTime,bo.getStartTime()+" 00:00:00")
                .le(org.springframework.util.StringUtils.hasText(bo.getEndTime()), ErpPurchaseOrder::getOrderTime,bo.getEndTime()+" 23:59:59")
                ;

        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<ErpPurchaseOrder> pages = erpPurchaseOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (ErpPurchaseOrder order:pages.getRecords()) {
                order.setItemList(erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, order.getId())));

            }
        }

        return PageResult.build(pages);
    }

    @Override
    public ErpPurchaseOrder getDetailById(Long id) {
        ErpPurchaseOrder order = erpPurchaseOrderMapper.selectById(id);
        if(order!=null){
            order.setItemList(erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, order.getId())));
            return order;
        }else
            return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int createPurchaseOrder(PurchaseOrderAddBo addBo) {
        if(addBo.getGoodsList() == null || addBo.getGoodsList().isEmpty()) return -1;
        // 添加主表
        ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();

        erpPurchaseOrder.setOrderNum("PUR"+ DateUtils.parseDateToStr("yyyyMMddHHmmss",new Date()));
        erpPurchaseOrder.setOrderAmount(addBo.getOrderAmount());
        erpPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        erpPurchaseOrder.setOrderDate(addBo.getOrderDate());
        erpPurchaseOrder.setSupplierId(addBo.getContactId());
        erpPurchaseOrder.setOrderTime(System.currentTimeMillis()/1000);
        erpPurchaseOrder.setCreateBy(addBo.getCreateBy());
        erpPurchaseOrder.setStatus(0);
        erpPurchaseOrder.setShipAmount(BigDecimal.ZERO);
        erpPurchaseOrderMapper.insert(erpPurchaseOrder);

        // 添加子表
        for (PurchaseOrderAddItemBo item:addBo.getGoodsList()) {
            ErpPurchaseOrderItem orderItem = new ErpPurchaseOrderItem();
            orderItem.setOrderDate(addBo.getOrderDate());
            orderItem.setOrderId(erpPurchaseOrder.getId());
            orderItem.setOrderNum(erpPurchaseOrder.getOrderNum());
            if(item.getAmount()!=null) {
                orderItem.setAmount(item.getAmount().doubleValue());
            }else{

                orderItem.setAmount(item.getPurPrice()==null?0.0:item.getPurPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue());
            }
            orderItem.setGoodsId(item.getGoodsId());
            orderItem.setGoodsNum(item.getNumber());
            orderItem.setIsDelete(0);
            orderItem.setPrice(item.getPurPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSpecId(item.getId());
            orderItem.setSpecNum(item.getSkuCode());
            orderItem.setStatus(0);
            orderItem.setTransType("Purchase");
            orderItem.setGoodsName(item.getGoodsName());
            orderItem.setColorValue(item.getColorValue());
            orderItem.setColorImage(item.getColorImage());
            orderItem.setSizeValue(item.getSizeValue());
            orderItem.setStyleValue(item.getStyleValue());

            erpPurchaseOrderItemMapper.insert(orderItem);
        }
        return 1;
    }

    @Override
    public int updateScmPurchaseOrder(PurchaseOrderOptionBo request) {
        ErpPurchaseOrder order = erpPurchaseOrderMapper.selectById(request.getId());
        if(order == null) return -1;


        if(request.getOptionType().equals("audit")){
            if(order.getStatus() !=0){
                // 状态不是待审核的
                return -1;
            }
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setAuditUser(request.getAuditUser());
            erpPurchaseOrder.setAuditTime(System.currentTimeMillis()/1000);
            erpPurchaseOrder.setRemark(request.getRemark());
            erpPurchaseOrder.setStatus(1);
            return erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        else if (request.getOptionType().equals("confirm")) {
            if(order.getStatus() !=1){
                // 状态不是已审核的不能确认
                return -1;
            }
            // 查询数据
            List<ErpPurchaseOrderItem> items = erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, request.getId()));

            Map<Long, List<ErpPurchaseOrderItem>> goodsGroup = items.stream().collect(Collectors.groupingBy(x -> x.getGoodsId()));
            Long total = items.stream().mapToLong(ErpPurchaseOrderItem::getQuantity).sum();
            // 生成费用信息
//            ScmPurchaseOrderCost cost = new ScmPurchaseOrderCost();
//            cost.setId(order.getId());
//            cost.setOrderId(order.getId());
//            cost.setSupplierId(order.getSupplierId());
//            cost.setOrderNum(order.getOrderNum());
//            cost.setOrderDate(order.getOrderDate());
//            cost.setOrderGoodsUnit(goodsGroup.size());
//            cost.setOrderSpecUnit(items.size());
//            cost.setOrderSpecUnitTotal(total.intValue());
//            cost.setOrderAmount(order.getOrderAmount());
//            cost.setActualAmount(request.getTotalAmount());
//            cost.setFreight(BigDecimal.ZERO);
//            cost.setConfirmUser(request.getConfirmUser());
//            cost.setConfirmTime(new Date());
//            cost.setCreateBy(request.getUpdateBy());
//            cost.setPayAmount(BigDecimal.ZERO);
//            cost.setPayCount(0);
//            cost.setStatus(0);
//            costMapper.insert(cost);

            // 更新主表
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setStatus(101);
            erpPurchaseOrder.setSupplierConfirmTime(new Date());
            erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        else if (request.getOptionType().equals("SupplierShip")) {
            if(order.getStatus() !=101 && order.getStatus()!=1){
                // 状态不是已确认的不能发货
                return -1;
            }
            // 查询数据
            List<ErpPurchaseOrderItem> items = erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, request.getId()));

            Map<Long, List<ErpPurchaseOrderItem>> goodsGroup = items.stream().collect(Collectors.groupingBy(x -> x.getGoodsId()));
            Long total = items.stream().mapToLong(ErpPurchaseOrderItem::getQuantity).sum();

            // 生成物流信息
            ErpPurchaseOrderShip ship = new ErpPurchaseOrderShip();

            ship.setId(order.getId());
            ship.setOrderId(order.getId());
            ship.setSupplierId(order.getSupplierId());
            ship.setOrderNum(order.getOrderNum());
            ship.setOrderDate(order.getOrderDate());
            ship.setOrderGoodsUnit(goodsGroup.size());
            ship.setOrderSpecUnit(items.size());
            ship.setOrderSpecUnitTotal(total.intValue());
            ship.setShipCompany(request.getShipCompany());
            ship.setShipNum(request.getShipNo());
            ship.setFreight(request.getShipCost());
            ship.setShipTime(request.getSupplierDeliveryTime());
            ship.setCreateBy(request.getUpdateBy());
            ship.setCreateTime(new Date());
            ship.setStatus(0);
            ship.setBackCount(0);
            ship.setStockInCount(0);
            shipMapper.insert(ship);
            // 更新费用表


//            ScmSupplier scmSupplier = supplierMapper.selectScmSupplierById(order.getContactId());
            // 生成应付信息fms_payable_purchase
//            ScmPurchaseOrderPayable payable = new ScmPurchaseOrderPayable();
//            payable.setSupplierId(order.getSupplierId());
////            fmsPP.setSupplierName(scmSupplier!=null ? scmSupplier.getName():"数据库未找到供应商信息");
//            payable.setAmount(order.getOrderAmount().add(request.getShipCost()));
//            payable.setDate(new Date());
//            payable.setPurchaseOrderNum(order.getOrderNum());
//            payable.setPurchaseDesc("{采购商品总数量:"+total+",不同款式:"+goodsGroup.size()+",不同SKU:"+items.size()+",商品总价:"+order.getOrderAmount()+",运费:"+request.getShipCost()+"}");
//            payable.setStatus(0);
//            payable.setCreateTime(new Date());
//            payable.setCreateBy(request.getUpdateBy());
//            payableMapper.insert(payable);

            // 更新主表
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setStatus(102);
            erpPurchaseOrder.setSupplierDeliveryTime(new Date());
            erpPurchaseOrder.setShipAmount(request.getShipCost());
            erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        return 1;
    }
}




