package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.ResultVoEnum;
import cn.qihangerp.common.utils.DateUtils;
import cn.qihangerp.mapper.ErpStockOutMapper;
import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.model.entity.OGoodsInventoryBatch;
import cn.qihangerp.model.entity.ErpStockOut;
import cn.qihangerp.model.entity.ErpStockOutItem;
import cn.qihangerp.model.entity.ErpStockOutItemDetail;
import cn.qihangerp.model.request.GoodsSkuInventoryVo;
import cn.qihangerp.model.request.StockOutCreateRequest;
import cn.qihangerp.model.request.StockOutItemRequest;
import cn.qihangerp.mapper.ErpStockOutItemDetailMapper;
import cn.qihangerp.service.ErpStockOutItemService;
import cn.qihangerp.service.ErpStockOutService;
import cn.qihangerp.service.OGoodsInventoryBatchService;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author qilip
* @description 针对表【wms_stock_out(出库单)】的数据库操作Service实现
* @createDate 2024-09-22 11:13:23
*/
@AllArgsConstructor
@Service
public class ErpStockOutServiceImpl extends ServiceImpl<ErpStockOutMapper, ErpStockOut>
    implements ErpStockOutService {
    private final ErpStockOutMapper outMapper;
    private final ErpStockOutItemService outItemService;
    private final ErpStockOutItemDetailMapper outItemPositionMapper;
    private final OGoodsInventoryBatchService goodsInventoryBatchService;
    private final OGoodsInventoryService goodsInventoryService;

    @Override
    public PageResult<ErpStockOut> queryPageList(ErpStockOut bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpStockOut> queryWrapper = new LambdaQueryWrapper<ErpStockOut>()

                .eq( bo.getStatus()!=null, ErpStockOut::getStatus, bo.getStatus())
                .eq( bo.getStockOutType()!=null, ErpStockOut::getStockOutType, bo.getStockOutType())
                .eq(StringUtils.isNotBlank(bo.getStockOutNum()), ErpStockOut::getStockOutNum, bo.getStockOutNum())
                .eq(StringUtils.isNotBlank(bo.getSourceNum()), ErpStockOut::getSourceNum, bo.getSourceNum())
                .eq(bo.getSourceId()!=null, ErpStockOut::getSourceId, bo.getSourceId())
                ;

        Page<ErpStockOut> pages = outMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }


    @Transactional
    @Override
    public ResultVo<Long> createEntry(Long userId, String userName, StockOutCreateRequest request) {
        if(request.getType() == null ) return ResultVo.error(ResultVoEnum.ParamsError,"缺少参数type");
        if(request.getItemList().isEmpty()) return ResultVo.error(ResultVoEnum.ParamsError,"缺少参数itemList");
        if(StringUtils.isBlank(request.getOutNum())){
            request.setOutNum(DateUtils.parseDateToStr("yyyyMMddHHmmss",new Date()));
        }
        if(StringUtils.isBlank(request.getOperator())){
            request.setOperator(userName);
        }

        Map<Long, List<GoodsSkuInventoryVo>> goodsGroup = request.getItemList().stream().collect(Collectors.groupingBy(x -> x.getGoodsId()));
        Long total = request.getItemList().stream().mapToLong(GoodsSkuInventoryVo::getQuantity).sum();

        //添加主表信息
        ErpStockOut insert = new ErpStockOut();

        insert.setStockOutNum(request.getOutNum());
        insert.setStockOutType(request.getType());
        insert.setSourceNum(request.getSourceNo());
        insert.setSourceId("0");
        insert.setRemark(request.getRemark());
        insert.setCreateBy(userName);
        insert.setCreateTime(new Date());
        insert.setGoodsUnit(goodsGroup.size());
        insert.setSpecUnit(request.getItemList().size());
        insert.setSpecUnitTotal(total.intValue());
        insert.setOutTotal(0);
        insert.setOperatorId(userId);
        insert.setOperatorName(StringUtils.isEmpty(request.getOperator())?userName:request.getOperator());
        insert.setPrintStatus(0);
        insert.setRemark(request.getRemark());
        insert.setStatus(0);//状态（0待入库1部分入库2全部入库）
        outMapper.insert(insert);

        //添加子表信息
        List<ErpStockOutItem> itemList = new ArrayList<>();
        for(GoodsSkuInventoryVo item: request.getItemList()) {

            ErpStockOutItem outItem = new ErpStockOutItem();

            outItem.setEntryId(insert.getId());
            outItem.setEntryNum(insert.getStockOutNum());
            outItem.setStockOutType(request.getType());
            outItem.setSourceOrderId("0");
            outItem.setSourceOrderItemId("0");
            outItem.setSourceOrderNum("");
            outItem.setGoodsId(item.getGoodsId());
            outItem.setGoodsTitle(item.getGoodsName());
            outItem.setGoodsImg(item.getGoodsImg());
            outItem.setGoodsNum(item.getGoodsNum());
            outItem.setGoodsSkuId(item.getSkuId());
            outItem.setSkuCode(item.getSkuCode());
            outItem.setSkuName(item.getSkuName());
//            inItem.setPurPrice(item.getPurPrice());
//            inItem.setSkuId(item.getSkuId());
//            inItem.setSkuCode(item.getSkuCode());
//            inItem.setGoodsName(item.getGoodsName());
//            inItem.setGoodsNum(item.getGoodsNum());
//            inItem.setSkuName(item.getSkuName());
//            inItem.setGoodsImage(item.getGoodsImg());
            outItem.setQuantity(item.getQuantity());
            outItem.setOutQuantity(0);
            outItem.setStatus(0);
            outItem.setCreateTime(new Date());
            itemList.add(outItem);


        }
        outItemService.saveBatch(itemList);
        return ResultVo.success();
    }

    @Override
    public ErpStockOut getDetailAndItemById(Long id) {
        ErpStockOut erpStockOut = outMapper.selectById(id);
        if(erpStockOut !=null){
            List<ErpStockOutItem> outItemList = outItemService.list(new LambdaQueryWrapper<ErpStockOutItem>().eq(ErpStockOutItem::getEntryId, id));
            if(outItemList!=null && outItemList.size()>0){
                // 查找outItem skuid相对应的库存批次list
                for(ErpStockOutItem item: outItemList){
//                    item.setOutQuantity(item.getQuantity()-item.getOutQuantity());
                    List<OGoodsInventoryBatch> erpGoodsInventoryBatches = goodsInventoryBatchService.querySkuBatchList(item.getGoodsSkuId());
                    item.setInventoryBatchList(erpGoodsInventoryBatches);
                }

            }
            erpStockOut.setItemList(outItemList);
        }
        return erpStockOut;
    }

    @Transactional
    @Override
    public ResultVo<Long> stockOut(Long userId, String userName, StockOutItemRequest request) {
        if(request.getEntryItemId() == null) return ResultVo.error(1500,"缺少参数：outItemId");
        if(request.getOutQty()==null || request.getOutQty().longValue()<=0) return ResultVo.error(1500,"缺少参数：出库数量");

        ErpStockOutItem outItem = outItemService.getById(request.getEntryItemId());
        if(outItem == null) return ResultVo.error(1500,"出库数据错误");
        if(outItem.getStatus()==2) return ResultVo.error("已出库了！不能重复操作！");
        // 判断库存够不够扣减的
        OGoodsInventoryBatch batch = goodsInventoryBatchService.getById(request.getInventoryBatchId());
        if(batch == null) return ResultVo.error(1500,"库存数据不存在");
        if(batch.getCurrentQty().longValue()< request.getOutQty().longValue())
            return ResultVo.error(1500,"库存不足");
        if(StringUtils.isEmpty(batch.getRemark())) batch.setRemark("");
        // 扣减库存
        // 1扣减批次库存
        OGoodsInventoryBatch updateBatch = new OGoodsInventoryBatch();
//        updateBatch.setCurrentQty(batch.getCurrentQty() - request.getOutQty());
        updateBatch.setUsableQty(batch.getUsableQty() - request.getOutQty());
        updateBatch.setUpdateBy(userName);
        updateBatch.setUpdateTime(new Date());
        updateBatch.setRemark(batch.getRemark()+"出库扣减库存；");
        updateBatch.setId(batch.getId());
        goodsInventoryBatchService.updateById(updateBatch);
        // 2扣减总库存
        OGoodsInventory goodsInventory = goodsInventoryService.getById(batch.getInventoryId());
        OGoodsInventory updateInventory = new OGoodsInventory();
        updateInventory.setId(goodsInventory.getId());
        updateInventory.setQuantity(goodsInventory.getQuantity() - request.getOutQty());
        updateInventory.setUpdateBy(userName);
        updateInventory.setUpdateTime(new Date());
        goodsInventoryService.updateById(updateInventory);


        // 添加item
        ErpStockOutItemDetail outItemPosition = new ErpStockOutItemDetail();

        outItemPosition.setEntryId(outItem.getEntryId());
        outItemPosition.setEntryItemId(outItem.getId());
        outItemPosition.setInventoryId(batch.getInventoryId());
        outItemPosition.setInventoryBatchId(batch.getId());
        outItemPosition.setQuantity(request.getOutQty());
        outItemPosition.setOperatorId(userId);
        outItemPosition.setOperatorName(userName);
        outItemPosition.setOutTime(new Date());
        outItemPosition.setWarehouseId(batch.getWarehouseId());
        outItemPosition.setWarehouseName(batch.getWarehouseName());
        outItemPosition.setPositionId(batch.getPositionId());
        outItemPosition.setPositionNum(batch.getPositionNum());
        outItemPosition.setRemark(request.getRemark());
        outItemPosition.setGoodsId(goodsInventory.getGoodsId());
        outItemPosition.setGoodsTitle(goodsInventory.getGoodsName());
        outItemPosition.setGoodsImg(goodsInventory.getGoodsImg());
        outItemPosition.setGoodsNum(goodsInventory.getGoodsNum());
        outItemPosition.setGoodsSkuId(goodsInventory.getSkuId());
        outItemPosition.setSkuCode(goodsInventory.getSkuCode());
        outItemPosition.setSkuName(goodsInventory.getSkuName());
        outItemPositionMapper.insert(outItemPosition);



        // 更新自己的状态
        ErpStockOutItem outItemUpdate = new ErpStockOutItem();
        outItemUpdate.setId(outItem.getId());
        outItemUpdate.setStatus(2);
        outItemUpdate.setCompleteTime(new Date());
        outItemUpdate.setOutQuantity(outItem.getOutQuantity()+ request.getOutQty());
        outItemUpdate.setUpdateTime(new Date());
        outItemService.updateById(outItemUpdate);

        // 更新主表单数据
        ErpStockOut erpStockOut = outMapper.selectById(outItem.getEntryId());
        if(erpStockOut.getOutTotal()==null) erpStockOut.setOutTotal(0);
        // 查询入库表单是否入库完成
        List<ErpStockOutItem> itemList = outItemService.list(new LambdaQueryWrapper<ErpStockOutItem>()
                .eq(ErpStockOutItem::getEntryId,outItem.getEntryId())
                .ne(ErpStockOutItem::getStatus, 2));
        ErpStockOut sUpdate = new ErpStockOut();
        if (itemList.isEmpty()) {
            // 全部入库完成了
            sUpdate.setStatus(2);
            sUpdate.setCompleteTime(new Date());
        } else {
            // 部分入库
            sUpdate.setStatus(1);
        }

        sUpdate.setId(outItem.getEntryId());
        sUpdate.setOperatorId(userId);
        sUpdate.setOperatorName(userName);
        sUpdate.setOutTime(new Date());
        sUpdate.setOutTotal(erpStockOut.getOutTotal()+request.getOutQty().intValue());
        sUpdate.setUpdateBy(userName);
        sUpdate.setUpdateTime(new Date());
        outMapper.updateById(sUpdate);

        return ResultVo.success();
    }
}




