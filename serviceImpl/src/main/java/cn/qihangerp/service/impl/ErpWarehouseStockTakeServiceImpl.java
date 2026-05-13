package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.enums.EnumStockOutType;
import cn.qihangerp.common.utils.DateUtils;
import cn.qihangerp.mapper.*;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.enums.EnumStockInType;
import cn.qihangerp.model.request.WarehouseStockTakeAddItemRequest;
import cn.qihangerp.model.request.WarehouseStockTakeCreateRequest;
import cn.qihangerp.service.ErpWarehouseStockTakeService;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author qihang
* @description 针对表【erp_warehouse_stock_take(仓库盘点表)】的数据库操作Service实现
* @createDate 2026-01-08 10:44:18
*/
@AllArgsConstructor
@Service
public class ErpWarehouseStockTakeServiceImpl extends ServiceImpl<ErpWarehouseStockTakeMapper, ErpWarehouseStockTake>
    implements ErpWarehouseStockTakeService {
    private final ErpWarehouseMapper warehouseMapper;
    private final ErpWarehouseStockTakeItemMapper stockTakeItemMapper;
    private final ErpStockInMapper stockInMapper;
    private final ErpStockInItemMapper stockInItemMapper;
    private final ErpStockOutMapper stockOutMapper;
    private final ErpStockOutItemMapper stockOutItemMapper;

    private final OGoodsSkuMapper goodsSkuMapper;
    private final OGoodsInventoryService goodsInventoryService;

    @Override
    public PageResult<ErpWarehouseStockTake> queryPageList(ErpWarehouseStockTake bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpWarehouseStockTake> queryWrapper = new LambdaQueryWrapper<ErpWarehouseStockTake>()
                .eq(bo.getWarehouseId()!=null, ErpWarehouseStockTake::getWarehouseId,bo.getWarehouseId())
                .eq( bo.getStatus()!=null, ErpWarehouseStockTake::getStatus, bo.getStatus())
                .eq(StringUtils.hasText(bo.getStockTakeDate()), ErpWarehouseStockTake::getStockTakeDate, bo.getStockTakeDate())
                ;

        Page<ErpWarehouseStockTake> pages = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }

    @Override
    public ErpWarehouseStockTake getDetailAndItemById(Long id) {
        ErpWarehouseStockTake erpStockIn = this.baseMapper.selectById(id);
        if(erpStockIn !=null){
            erpStockIn.setItemList(stockTakeItemMapper.selectList(new LambdaQueryWrapper<ErpWarehouseStockTakeItem>()
                    .eq(ErpWarehouseStockTakeItem::getStockTakeId,id)));
            return erpStockIn;
        }else
            return null;
    }

    @Override
    public ResultVo createEntry(Long userId, String userName, WarehouseStockTakeCreateRequest request) {
        ErpWarehouse erpWarehouse = warehouseMapper.selectById(request.getWarehouseId());
        if(erpWarehouse == null){
            return ResultVo.error("没有找到仓库");
        }
        ErpWarehouseStockTake stockTake = new ErpWarehouseStockTake();
        stockTake.setStockTakeDate(DateUtils.getDate());
        stockTake.setStockTakeMan(userName);
        stockTake.setSkuUnit(0);
        stockTake.setTotalQuantity(0);
        stockTake.setResultQuantity(0);
        stockTake.setRemark(request.getRemark());
        stockTake.setWarehouseId(request.getWarehouseId());
        stockTake.setWarehouseName(erpWarehouse.getName());

        stockTake.setStatus(0);
        stockTake.setCreateTime(new Date());
        stockTake.setCreateBy(userName);
        this.baseMapper.insert(stockTake);
        return ResultVo.success();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo addItem(Long userId, String userName, WarehouseStockTakeAddItemRequest request) {
        ErpWarehouseStockTake stockTake = this.baseMapper.selectById(request.getId());
        if(stockTake == null){
            return ResultVo.error("数据不存在");
        }else if(stockTake.getStatus()!=0 && stockTake.getStatus()!=1){
            return ResultVo.error("已完成的不允许操作");
        }
        if(request.getItemList()==null||request.getItemList().size()==0){
            return ResultVo.error("没有数据");
        }
        int skuTotal = 0;
        int panyingUnit = 0;//盘盈数量
        int pankuiUnit = 0;//盘亏数量
        int totalStock = 0;
        int resultTotal = 0;
        for (var item : request.getItemList()) {
            skuTotal++;
            ErpWarehouseStockTakeItem oldItem = stockTakeItemMapper.selectById(item.getId());

//            List<ErpWarehouseStockTakeItem> erpWarehouseStockTakeItems = stockTakeItemMapper.selectList(new LambdaQueryWrapper<ErpWarehouseStockTakeItem>()
//                    .eq(ErpWarehouseStockTakeItem::getStockTakeId, stockTake.getId())
//                    .eq(ErpWarehouseStockTakeItem::getWarehouseId, stockTake.getWarehouseId())
//                    .eq(ErpWarehouseStockTakeItem::getGoodsId, item.getId()));
//            if(erpWarehouseStockTakeItems!=null && erpWarehouseStockTakeItems.size()>0){
            if(oldItem!=null){
                // 查库存
//                Long goodsStockQty = goodsInventoryService.getGoodsSkuStockQty(item.getSkuId(),stockTake.getWarehouseId());
//                totalStock += goodsStockQty.intValue();
                totalStock += oldItem.getQuantity();

                resultTotal+=item.getQuantity().intValue();
                //更新
                ErpWarehouseStockTakeItem stockTakeItemUpdate = new ErpWarehouseStockTakeItem();
                stockTakeItemUpdate.setId(oldItem.getId());

//                stockTakeItemUpdate.setQuantity(goodsStockQty.intValue());
                stockTakeItemUpdate.setTakeQuantity(item.getQuantity());
                //盘点结果（0未出结果10盘平20盘盈30盘亏）
                int result = 0;
                int resultQty = 0;
                if (oldItem.getQuantity() == item.getQuantity().intValue()) {
                    result = 10;
                    resultQty=0;
                } else if (oldItem.getQuantity() < item.getQuantity().intValue()) {
                    result = 20;
                    panyingUnit++;
                    resultQty = item.getQuantity()-oldItem.getQuantity();
                } else if (oldItem.getQuantity() > item.getQuantity().intValue()) {
                    result = 30;
                    resultQty = oldItem.getQuantity() - item.getQuantity().intValue();
                    pankuiUnit++;
                }
                stockTakeItemUpdate.setResult(result);
                stockTakeItemUpdate.setResultQty(resultQty);
                stockTakeItemUpdate.setStatus(2);
                stockTakeItemUpdate.setUpdateTime(new Date());
                stockTakeItemUpdate.setUpdateBy(userName);
                stockTakeItemMapper.updateById(stockTakeItemUpdate);
            }else {
                // 新增
                // 查商品
                OGoodsSku goodsSku = goodsSkuMapper.selectById(item.getId());
                if (goodsSku == null) {
                    return ResultVo.error("商品Sku不存在！" + item.getId());
                }
                if (item.getQuantity() == null) item.setQuantity(0);
                // 查库存
                Long goodsStockQty = goodsInventoryService.getGoodsSkuStockQty(item.getId(),stockTake.getWarehouseId());
                totalStock += goodsStockQty.intValue();
                resultTotal+=item.getQuantity().intValue();

                ErpWarehouseStockTakeItem stockTakeItem = new ErpWarehouseStockTakeItem();
                stockTakeItem.setStockTakeId(stockTake.getId());
                stockTakeItem.setWarehouseId(stockTake.getWarehouseId());
                stockTakeItem.setGoodsId(Long.parseLong(goodsSku.getGoodsId()));
                stockTakeItem.setGoodsNum("");
                stockTakeItem.setGoodsName(goodsSku.getGoodsName());
                stockTakeItem.setGoodsImage(goodsSku.getColorImage());
                stockTakeItem.setSkuId(Long.parseLong(goodsSku.getId()));
                stockTakeItem.setSkuCode(goodsSku.getSkuCode());
                stockTakeItem.setSkuName(goodsSku.getSkuName());
                stockTakeItem.setQuantity(goodsStockQty.intValue());
                stockTakeItem.setTakeQuantity(item.getQuantity());
                //盘点结果（0未出结果10盘平20盘盈30盘亏）
                int result = 0;
                int resultQty = 0;
                if (goodsStockQty.intValue() == item.getQuantity().intValue()) {
                    result = 10;
                    resultQty=0;
                } else if (goodsStockQty.intValue()< item.getQuantity().intValue()) {
                    result = 20;
                    resultQty = item.getQuantity()-goodsStockQty.intValue();
                    panyingUnit++;
                } else if (goodsStockQty.intValue() > item.getQuantity().intValue()) {
                    result = 30;
                    resultQty = goodsStockQty.intValue() - item.getQuantity().intValue();
                    pankuiUnit++;
                }
                stockTakeItem.setResult(result);
                stockTakeItem.setResultQty(resultQty);
                stockTakeItem.setStatus(2);
                stockTakeItem.setCreateTime(new Date());
                stockTakeItem.setCreateBy(userName);
                stockTakeItemMapper.insert(stockTakeItem);
            }
        }

        // 更新自己
        ErpWarehouseStockTake update = new ErpWarehouseStockTake();
        update.setId(stockTake.getId());
        update.setSkuUnit(skuTotal);
        update.setPanyingUnit(panyingUnit);
        update.setPankuiUnit(pankuiUnit);
        update.setTotalQuantity(totalStock);
        update.setResultQuantity(resultTotal);
        update.setStatus(1);
        if(stockTake.getFirstTakeTime()==null){
            update.setFirstTakeTime(new Date());
        }
        update.setUpdateTime(new Date());
        update.setUpdateBy(userName);
        this.baseMapper.updateById(update);
        return ResultVo.success();
    }

    /**
     * 盘点完成
     * 1、生成盘盈入库单，生成盘亏出库单
     * 2、修改状态
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo complete(Long id,Long userId,String userName) {
        ErpWarehouseStockTake stockTake = this.baseMapper.selectById(id);
        if(stockTake == null){
            return ResultVo.error("数据不存在");
        }else if(stockTake.getStatus()!=0 && stockTake.getStatus()!=1){
            return ResultVo.error("已完成的不允许操作");
        }
        List<ErpWarehouseStockTakeItem> items = stockTakeItemMapper.selectList(new LambdaQueryWrapper<ErpWarehouseStockTakeItem>().eq(ErpWarehouseStockTakeItem::getStockTakeId, id));
        if(items.isEmpty()) return ResultVo.error("没有找到盘点数据");

        // 盘盈list
        List<ErpWarehouseStockTakeItem> panying = items.stream()
                .filter(item -> item.getResult() ==20)
                .collect(Collectors.toList());
        if(!panying.isEmpty()){
            Integer takeQty =  panying.stream().mapToInt(x -> x.getTakeQuantity()).sum();
            Integer qty = panying.stream().mapToInt(ErpWarehouseStockTakeItem::getQuantity).sum();
            // 添加盘盈入库单
            //添加主表信息
            ErpStockIn stockIn = new ErpStockIn();
            stockIn.setStockInNum("PYRK-" + DateUtils.parseDateToStr("yyyyMMdd", new Date()) + "-" + System.currentTimeMillis() / 1000);
            stockIn.setStockInType(EnumStockInType.TAKE_STOCK_IN.getIndex());
            stockIn.setStockInOperator(userName);
            stockIn.setStockInOperatorId(userId.toString());
//        insert.setStockInTime(new Date());
            stockIn.setSourceId(stockTake.getId());
            stockIn.setSourceNo(stockTake.getId().toString());
            stockIn.setRemark("盘盈生成入库单");
            stockIn.setCreateBy(userName);
            stockIn.setCreateTime(new Date());
            stockIn.setSourceGoodsUnit(panying.size());
            stockIn.setSourceSpecUnit(panying.size());
            stockIn.setSourceSpecUnitTotal(takeQty-qty);
            stockIn.setStatus(0);//状态（0待入库1部分入库2全部入库）
            int total=0;
            List<ErpStockInItem> inItemList = new ArrayList<>();

            for(ErpWarehouseStockTakeItem item : panying){
                ErpStockInItem inItem = new ErpStockInItem();
                inItem.setGoodsId(item.getGoodsId().toString());
                inItem.setGoodsName(item.getGoodsName());
                inItem.setGoodsImage(item.getGoodsImage());
                inItem.setGoodsNum(item.getGoodsNum());
                inItem.setSkuId(item.getSkuId().toString());
                inItem.setSkuCode(item.getSkuCode());
                inItem.setSkuName(item.getSkuName());
                inItem.setQuantity(item.getTakeQuantity()-item.getQuantity());
                inItem.setInQuantity(0);
                inItem.setStatus(0);
                inItem.setCreateBy(userName);
                inItem.setCreateTime(new Date());
                inItemList.add(inItem);
                total+=inItem.getQuantity();
            }
            stockIn.setSourceGoodsUnit(panying.size());
            stockIn.setSourceSpecUnit(panying.size());
            stockIn.setSourceSpecUnitTotal(total);
            stockInMapper.insert(stockIn);
            for(ErpStockInItem item : inItemList){
                item.setStockInId(stockIn.getId());
                stockInItemMapper.insert(item);
            }

        }

        // 盘亏list
        List<ErpWarehouseStockTakeItem> pankui = items.stream()
                .filter(item -> item.getResult() ==30)
                .collect(Collectors.toList());
        if(!pankui.isEmpty()){
            // 添加盘亏出库单
            Integer takeQty =  pankui.stream().mapToInt(x -> x.getTakeQuantity()).sum();
            Integer qty = pankui.stream().mapToInt(ErpWarehouseStockTakeItem::getQuantity).sum();
            //添加主表信息
            ErpStockOut stockOut = new ErpStockOut();
            stockOut.setStockOutNum("PKCK-" + DateUtils.parseDateToStr("yyyyMMdd", new Date()) + "-" + System.currentTimeMillis() / 1000);
            stockOut.setStockOutType(EnumStockOutType.TAKE_STOCK_OUT.getIndex());
            stockOut.setSourceNum(stockTake.getId().toString());
            stockOut.setSourceId(stockTake.getId().toString());
            stockOut.setRemark("盘亏生成出库单");
            stockOut.setCreateBy(userName);
            stockOut.setCreateTime(new Date());
            stockOut.setGoodsUnit(pankui.size());
            stockOut.setSpecUnit(pankui.size());
            stockOut.setOutTotal(0);
            stockOut.setOperatorId(userId);
            stockOut.setOperatorName(userName);
            stockOut.setPrintStatus(0);
            stockOut.setStatus(0);//状态（0待入库1部分入库2全部入库）
            int total=0;
            List<ErpStockOutItem> outItemList = new ArrayList<>();
            for(ErpWarehouseStockTakeItem item : pankui){
                ErpStockOutItem inItem = new ErpStockOutItem();
                inItem.setSourceOrderId(stockTake.getId().toString());
                inItem.setSourceOrderNum(stockTake.getId().toString());
                inItem.setSourceOrderItemId(item.getId().toString());
                inItem.setSourceSubOrderNum(item.getId().toString());
//                inItem.setEntryId(stockOut.getId());
                inItem.setStockOutType(stockOut.getStockOutType());
                inItem.setGoodsId(item.getGoodsId());
                inItem.setGoodsTitle(item.getGoodsName());
                inItem.setGoodsNum(item.getGoodsNum());
                inItem.setGoodsImg(item.getGoodsImage());
                inItem.setGoodsSkuId(item.getSkuId());
                inItem.setSkuCode(item.getSkuCode());
                inItem.setSkuName(item.getSkuName());
                inItem.setQuantity(item.getQuantity()-item.getTakeQuantity());
                inItem.setOutQuantity(0);
                inItem.setStatus(0);
                inItem.setCreateTime(new Date());
                outItemList.add(inItem);
                total+=inItem.getQuantity();
            }
            stockOut.setSpecUnitTotal(total);
            stockOutMapper.insert(stockOut);
            for(ErpStockOutItem item : outItemList){
                item.setEntryId(stockOut.getId());
                item.setEntryNum(stockOut.getStockOutNum());
                stockOutItemMapper.insert(item);
            }
        }

        // 更新自己
        ErpWarehouseStockTake update = new ErpWarehouseStockTake();
        update.setId(stockTake.getId());
        update.setUpdateTime(new Date());
        update.setUpdateBy(userName);
        update.setSkuUnit(items.size());
        update.setPanyingUnit(panying.size());
        update.setPankuiUnit(pankui.size());
        update.setTotalQuantity(items.stream().mapToInt(item -> item.getQuantity()).sum());
        update.setResultQuantity(items.stream().mapToInt(item -> item.getTakeQuantity()).sum());
        update.setStatus(2);
        update.setCompleteTime(new Date());
        update.setRemark("生成出入库单");
        this.baseMapper.updateById(update);
        return ResultVo.success();
    }
}




