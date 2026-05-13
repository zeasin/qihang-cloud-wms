package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpStockOutItemDetail;
import cn.qihangerp.mapper.ErpStockOutItemDetailMapper;
import cn.qihangerp.service.ErpStockOutItemDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
* @author qilip
* @description 针对表【wms_stock_out_item_position(出库仓位详情)】的数据库操作Service实现
* @createDate 2024-09-22 11:13:23
*/
@Service
public class ErpStockOutItemDetailServiceImpl extends ServiceImpl<ErpStockOutItemDetailMapper, ErpStockOutItemDetail>
    implements ErpStockOutItemDetailService {

    @Override
    public PageResult<ErpStockOutItemDetail> queryPageList(ErpStockOutItemDetail bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpStockOutItemDetail> queryWrapper = new LambdaQueryWrapper<ErpStockOutItemDetail>()
//                .eq( bo.getStatus()!=null, ErpStockOutItem::getStatus, bo.getStatus())
//                .eq( bo.getStockOutType()!=null, ErpStockOutItem::getStockOutType, bo.getStockOutType())
//                .eq( bo.getEntryId()!=null, ErpStockOutItem::getEntryId, bo.getEntryId())
//                .like(StringUtils.hasText(bo.getEntryNum()), ErpStockOutItem::getEntryNum, bo.getEntryNum())
                ;

        Page<ErpStockOutItemDetail> pages = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }
}




