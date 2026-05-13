package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.mapper.ErpMerchantMapper;
import cn.qihangerp.mapper.ErpWarehouseMerchantMapper;
import cn.qihangerp.model.bo.MerchantAddBo;
import cn.qihangerp.model.entity.ErpMerchant;
import cn.qihangerp.model.query.MerchantQuery;
import cn.qihangerp.service.ErpMerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author qilip
* @description 针对表【oms_tenant(租户用户表)】的数据库操作Service实现
* @createDate 2024-06-23 11:10:08
*/
@AllArgsConstructor
@Service
public class ErpMerchantServiceImpl extends ServiceImpl<ErpMerchantMapper, ErpMerchant>
    implements ErpMerchantService {
    private final ErpMerchantMapper mapper;
//    private final ErpVendorMapper vendorMapper;
    private final ErpWarehouseMerchantMapper warehouseMerchantMapper;
//    private final ErpMerchantSupplierService merchantSupplierService;


    @Override
    public PageResult<ErpMerchant> queryPageList(MerchantQuery bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpMerchant> queryWrapper = new LambdaQueryWrapper<ErpMerchant>()
                .eq(bo.getStatus()!=null,ErpMerchant::getStatus,bo.getStatus())
                .like(StringUtils.hasText(bo.getNumber()),ErpMerchant::getNumber,bo.getNumber())
                .like(StringUtils.hasText(bo.getUsci()),ErpMerchant::getUsci,bo.getUsci())
                .like(StringUtils.hasText(bo.getMobile()),ErpMerchant::getMobile,bo.getMobile())
                .eq(bo.getMerchantId()!=null,ErpMerchant::getId,bo.getMerchantId())
                .like(StringUtils.hasText(bo.getName()),ErpMerchant::getName,bo.getName());

        Page<ErpMerchant> pages = mapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(pages);
    }

    @Override
    public ErpMerchant selectUserByUserName(String userName) {
        List<ErpMerchant> scmDistributors = mapper.selectList(new LambdaQueryWrapper<ErpMerchant>()
                .eq(ErpMerchant::getLoginName, userName)
                .eq(ErpMerchant::getDelFlag, "0"));
        if(scmDistributors == null || scmDistributors.size()==0)
            return null;
        else
            return scmDistributors.get(0);
    }

    @Override
    public void updateByUserId(ErpMerchant tenant, Long userId) {
        tenant.setId(userId);
        mapper.updateById(tenant);
//        mapper.update(tenant,new LambdaQueryWrapper<OmsTenant>().eq(OmsTenant::getId,userId));
    }

    @Override
    public ResultVo<ErpMerchant> add(MerchantAddBo bo, String createBy) {
        ErpMerchant merchant = new ErpMerchant();
        BeanUtils.copyProperties(bo,merchant);
        merchant.setPassword(bo.getLoginPwd());
        merchant.setStatus("0");
        merchant.setDelFlag("0");
//        merchant.setLoginIp(IpUtils.getIpAddr());
//        merchant.setLoginDate(new Date());
        merchant.setCreateBy(createBy);
        merchant.setCreateTime(new Date());
        mapper.insert(merchant);
        return ResultVo.success(merchant);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<ErpMerchant> setLoginName(Long id,MerchantAddBo bo, String updateBy) {
        ErpMerchant merchant = mapper.selectById(id);
        if(merchant==null) return ResultVo.error("数据不存在");
         if(!merchant.getLoginName().equals(bo.getLoginName())){
             // 新登陆账号
             ErpMerchant merchant1 = this.selectUserByUserName(bo.getLoginName());
             if(merchant1!=null) return ResultVo.error("登录账号已存在，请重新输入");
         }
         ErpMerchant up = new ErpMerchant();
         up.setId(id);
         up.setLoginName(bo.getLoginName());
         up.setPassword(bo.getLoginPwd());
         up.setUpdateBy(updateBy);
         up.setUpdateTime(new Date());
         mapper.updateById(up);


        return ResultVo.success(merchant);
    }

}




