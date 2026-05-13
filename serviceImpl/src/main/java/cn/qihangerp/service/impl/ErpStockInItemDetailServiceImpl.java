package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.model.entity.ErpStockInItemDetail;
import cn.qihangerp.service.ErpStockInItemDetailService;
import cn.qihangerp.mapper.ErpStockInItemDetailMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【erp_stock_in_item_detail(出库仓位详情)】的数据库操作Service实现
* @createDate 2026-01-05 21:37:40
*/
@Service
public class ErpStockInItemDetailServiceImpl extends ServiceImpl<ErpStockInItemDetailMapper, ErpStockInItemDetail>
    implements ErpStockInItemDetailService{

    @Override
    public PageResult<ErpStockInItemDetail> queryPageList(ErpStockInItemDetail bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpStockInItemDetail> queryWrapper = new LambdaQueryWrapper<ErpStockInItemDetail>()
                .eq( bo.getStockInItemId()!=null, ErpStockInItemDetail::getStockInItemId, bo.getStockInItemId())
                .eq(StringUtils.isNotBlank(bo.getStockInNum()), ErpStockInItemDetail::getStockInNum, bo.getStockInNum())
                .eq(StringUtils.isNotBlank(bo.getStockInSourceNo()), ErpStockInItemDetail::getStockInSourceNo, bo.getStockInSourceNo())
                .eq(bo.getStockInId()!=null, ErpStockInItemDetail::getStockInId, bo.getStockInId())
                ;

        Page<ErpStockInItemDetail> pages = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }
}




