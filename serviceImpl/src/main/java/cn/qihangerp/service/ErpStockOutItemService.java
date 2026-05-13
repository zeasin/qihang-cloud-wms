package cn.qihangerp.service;


import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpStockOutItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【wms_stock_out_item(出库单明细)】的数据库操作Service
* @createDate 2024-09-22 11:13:23
*/
public interface ErpStockOutItemService extends IService<ErpStockOutItem> {
    PageResult<ErpStockOutItem> queryPageList(ErpStockOutItem bo, PageQuery pageQuery);

    ErpStockOutItem getDetailAndInventoryBatchById(Long id);
}
