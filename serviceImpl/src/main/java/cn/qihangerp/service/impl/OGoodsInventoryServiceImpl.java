package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.mapper.OGoodsInventoryMapper;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author qilip
* @description 针对表【o_goods_inventory(商品库存表)】的数据库操作Service实现
* @createDate 2024-09-23 22:39:50
*/
@AllArgsConstructor
@Service
public class OGoodsInventoryServiceImpl extends ServiceImpl<OGoodsInventoryMapper, OGoodsInventory>
    implements OGoodsInventoryService {
    private final OGoodsInventoryMapper mapper;

    @Override
    public PageResult<OGoodsInventory> queryPageList(OGoodsInventory bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OGoodsInventory> queryWrapper = new LambdaQueryWrapper<OGoodsInventory>();
        queryWrapper.eq(bo.getGoodsId()!=null,OGoodsInventory::getGoodsId,bo.getGoodsId());
        queryWrapper.eq(bo.getSkuId()!=null,OGoodsInventory::getSkuId,bo.getSkuId());
        queryWrapper.eq(StringUtils.hasText(bo.getGoodsNum()),OGoodsInventory::getGoodsNum,bo.getGoodsNum());
        queryWrapper.eq(StringUtils.hasText(bo.getSkuCode()),OGoodsInventory::getSkuCode,bo.getSkuCode());

        Page<OGoodsInventory> pages = mapper.selectPage(pageQuery.build(), queryWrapper);

        return PageResult.build(pages);
    }

    @Override
    public long getAllInventoryQuantity() {
        return mapper.getAllInventoryQuantity();
    }

    @Override
    public Long getGoodsSkuStockQty(Long goodsSkuId,Long warehouseId) {
        List<OGoodsInventory> erpWarehouseGoodsStocks = this.baseMapper.selectList(
                new LambdaQueryWrapper<OGoodsInventory>()
                        .eq(OGoodsInventory::getSkuId, goodsSkuId)
                        .eq(OGoodsInventory::getWarehouseId, warehouseId));
        if (erpWarehouseGoodsStocks.isEmpty()) return 0L;
        return erpWarehouseGoodsStocks.stream().mapToLong(OGoodsInventory::getQuantity).sum();
    }
}




