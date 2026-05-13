package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.OGoodsInventoryBatch;
import cn.qihangerp.mapper.ErpStockOutItemMapper;
import cn.qihangerp.model.entity.ErpStockOutItem;
import cn.qihangerp.service.ErpStockOutItemService;
import cn.qihangerp.service.OGoodsInventoryBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author qilip
* @description 针对表【wms_stock_out_item(出库单明细)】的数据库操作Service实现
* @createDate 2024-09-22 11:13:23
*/
@AllArgsConstructor
@Service
public class ErpStockOutItemServiceImpl extends ServiceImpl<ErpStockOutItemMapper, ErpStockOutItem>
    implements ErpStockOutItemService {
    private final OGoodsInventoryBatchService goodsInventoryBatchService;
    @Override
    public PageResult<ErpStockOutItem> queryPageList(ErpStockOutItem bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpStockOutItem> queryWrapper = new LambdaQueryWrapper<ErpStockOutItem>()
                .eq( bo.getStatus()!=null, ErpStockOutItem::getStatus, bo.getStatus())
                .eq( bo.getStockOutType()!=null, ErpStockOutItem::getStockOutType, bo.getStockOutType())
                .eq( bo.getEntryId()!=null, ErpStockOutItem::getEntryId, bo.getEntryId())
                .like(StringUtils.hasText(bo.getEntryNum()), ErpStockOutItem::getEntryNum, bo.getEntryNum())
                ;

        Page<ErpStockOutItem> pages = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }

    @Override
    public ErpStockOutItem getDetailAndInventoryBatchById(Long id) {
        ErpStockOutItem erpStockOutItem = this.baseMapper.selectById(id);
        if(erpStockOutItem==null) return null;
        List<OGoodsInventoryBatch> erpGoodsInventoryBatches = goodsInventoryBatchService.querySkuBatchList(erpStockOutItem.getGoodsSkuId());
        erpStockOutItem.setInventoryBatchList(erpGoodsInventoryBatches);
        return erpStockOutItem;
    }
}




