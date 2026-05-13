package cn.qihangerp.service;


import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpStockOutItemDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【wms_stock_out_item_position(出库仓位详情)】的数据库操作Service
* @createDate 2024-09-22 11:13:23
*/
public interface ErpStockOutItemDetailService extends IService<ErpStockOutItemDetail> {
    PageResult<ErpStockOutItemDetail> queryPageList(ErpStockOutItemDetail bo, PageQuery pageQuery);
}
