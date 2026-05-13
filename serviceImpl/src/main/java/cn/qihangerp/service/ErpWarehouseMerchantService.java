package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpWarehouseMerchant;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author rich
* @description 针对表【erp_vendor_merchant(租户用户表)】的数据库操作Service
* @createDate 2025-06-19 09:12:28
*/
public interface ErpWarehouseMerchantService extends IService<ErpWarehouseMerchant> {
    PageResult<ErpWarehouseMerchant> queryPageList(ErpWarehouseMerchant bo, PageQuery pageQuery);
    List<ErpWarehouseMerchant> selectMerchantWarehouseList(Long merchantId,String warehouseType);
    List<ErpWarehouseMerchant> selectMerchantCloudWarehouseList(Long merchantId);
}
