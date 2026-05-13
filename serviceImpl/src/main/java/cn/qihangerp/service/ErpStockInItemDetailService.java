package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpStockInItemDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 1
* @description 针对表【erp_stock_in_item_detail(出库仓位详情)】的数据库操作Service
* @createDate 2026-01-05 21:37:40
*/
public interface ErpStockInItemDetailService extends IService<ErpStockInItemDetail> {
    PageResult<ErpStockInItemDetail> queryPageList(ErpStockInItemDetail bo, PageQuery pageQuery);
}
