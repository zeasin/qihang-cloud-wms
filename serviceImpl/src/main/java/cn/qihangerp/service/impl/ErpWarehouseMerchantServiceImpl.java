package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.ErpWarehouseMerchantMapper;
import cn.qihangerp.model.entity.ErpWarehouseMerchant;
import cn.qihangerp.service.ErpWarehouseMerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author rich
* @description 针对表【erp_vendor_merchant(租户用户表)】的数据库操作Service实现
* @createDate 2025-06-19 09:12:28
*/
@Service
public class ErpWarehouseMerchantServiceImpl extends ServiceImpl<ErpWarehouseMerchantMapper, ErpWarehouseMerchant>
    implements ErpWarehouseMerchantService {

    @Override
    public PageResult<ErpWarehouseMerchant> queryPageList(ErpWarehouseMerchant bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpWarehouseMerchant> queryWrapper = new LambdaQueryWrapper<ErpWarehouseMerchant>();
        queryWrapper.eq(ErpWarehouseMerchant::getWarehouseId, bo.getWarehouseId());
        Page<ErpWarehouseMerchant> pages = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }

    @Override
    public List<ErpWarehouseMerchant> selectMerchantWarehouseList(Long merchantId,String warehouseType) {
        LambdaQueryWrapper<ErpWarehouseMerchant> queryWrapper = new LambdaQueryWrapper<ErpWarehouseMerchant>()
        .eq(ErpWarehouseMerchant::getMerchantId, merchantId)
                .eq(StringUtils.hasText(warehouseType),ErpWarehouseMerchant::getWarehouseType,warehouseType);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<ErpWarehouseMerchant> selectMerchantCloudWarehouseList(Long merchantId) {
        LambdaQueryWrapper<ErpWarehouseMerchant> queryWrapper = new LambdaQueryWrapper<ErpWarehouseMerchant>()
                .eq(ErpWarehouseMerchant::getMerchantId, merchantId)
                .ne(ErpWarehouseMerchant::getWarehouseType,"LOCAL")
                .ne(ErpWarehouseMerchant::getWarehouseType,"OTHER");
        return this.baseMapper.selectList(queryWrapper);
    }
}




