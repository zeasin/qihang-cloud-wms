package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpWarehouseStockTake;
import cn.qihangerp.model.request.WarehouseStockTakeAddItemRequest;
import cn.qihangerp.model.request.WarehouseStockTakeCreateRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qihang
* @description 针对表【erp_warehouse_stock_take(仓库盘点表)】的数据库操作Service
* @createDate 2026-01-08 10:44:18
*/
public interface ErpWarehouseStockTakeService extends IService<ErpWarehouseStockTake> {
    PageResult<ErpWarehouseStockTake> queryPageList(ErpWarehouseStockTake bo, PageQuery pageQuery);
    ErpWarehouseStockTake getDetailAndItemById(Long id);
    ResultVo createEntry(Long userId, String userName, WarehouseStockTakeCreateRequest request);
    ResultVo addItem(Long userId, String userName, WarehouseStockTakeAddItemRequest request);
    ResultVo complete(Long id,Long userId,String userName);
}
