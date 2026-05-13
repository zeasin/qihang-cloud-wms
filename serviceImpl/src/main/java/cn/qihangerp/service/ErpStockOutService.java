package cn.qihangerp.service;


import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpStockOut;
import cn.qihangerp.model.request.StockOutCreateRequest;
import cn.qihangerp.model.request.StockOutItemRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【wms_stock_out(出库单)】的数据库操作Service
* @createDate 2024-09-22 11:13:23
*/
public interface ErpStockOutService extends IService<ErpStockOut> {
    PageResult<ErpStockOut> queryPageList(ErpStockOut bo, PageQuery pageQuery);
    ResultVo<Long> createEntry(Long userId, String userName, StockOutCreateRequest request);

    ErpStockOut getDetailAndItemById(Long id);

    ResultVo<Long> stockOut(Long userId, String userName, StockOutItemRequest request);
}
