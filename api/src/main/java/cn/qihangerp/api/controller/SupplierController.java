package cn.qihangerp.api.controller;

import cn.qihangerp.common.*;
import cn.qihangerp.common.enums.EnumUserType;
import cn.qihangerp.common.sys.ISysUserService;
import cn.qihangerp.model.entity.ErpSupplier;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.ErpSupplierService;
import cn.qihangerp.service.OShopService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@AllArgsConstructor
@RestController
@RequestMapping("/api/erp-api/supplier")
public class SupplierController extends BaseController {
    private final ErpSupplierService supplierService;
    private final ISysUserService userService;
    private final OShopService shopService;

    /**
     * 所有供应商list
     *
     * @param bo
     * @param pageQuery
     * @return
     */
    @GetMapping("/list_all")
    public TableDataInfo list_all(ErpSupplier bo, PageQuery pageQuery) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return getDataTable(new ArrayList<>());
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            // 商户
            bo.setMerchantId(SecurityUtils.getDeptId());
            var pageList = supplierService.list(new LambdaQueryWrapper<ErpSupplier>()
                    .eq(ErpSupplier::getIsDelete, 0)
                    .eq(ErpSupplier::getMerchantId, bo.getMerchantId())
                    .eq(ErpSupplier::getShopId, 0L)
            );
            return getDataTable(pageList);
        } else {
            return getDataTable(new ArrayList<>());
        }
    }

    @GetMapping("/list")
    public TableDataInfo list(ErpSupplier bo, PageQuery pageQuery) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0l;
        Long shopId = 0l;
        if (userIdentity == null) {
            // 登录用户错误
            return getDataTable(new ArrayList<>());
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            // 商户
            merchantId = SecurityUtils.getDeptId();
            shopId = 0l;
            bo.setMerchantId(merchantId);
            bo.setShopId(shopId);
            var pageList = supplierService.queryPageList(bo, pageQuery);
            return getDataTable(pageList);
        } else {
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return AjaxResult.error("未登录用户");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            return success(supplierService.getById(id));
        } else return AjaxResult.error("未登录用户");
    }

    /**
     * 新增【请填写功能名称】
     */
    @PostMapping
    public AjaxResult add(@RequestBody ErpSupplier scmSupplier) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0l;
        Long shopId = 0l;
        if (userIdentity == null) {
            // 登录用户错误
            return AjaxResult.error("未登录用户");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            // 商户
            merchantId = SecurityUtils.getDeptId();
            shopId = 0l;
            scmSupplier.setMerchantId(merchantId);
            scmSupplier.setShopId(shopId);
            scmSupplier.setCreateTime(new Date());
            scmSupplier.setIsDelete(0);
            return toAjax(supplierService.save(scmSupplier));
        } else {
            return AjaxResult.error("无权限操作");
        }
    }

    /**
     * 修改【请填写功能名称】
     */
    @PutMapping
    public AjaxResult edit(@RequestBody ErpSupplier scmSupplier) {
        if (scmSupplier.getId() == null) return AjaxResult.error("缺少参数：id");
        ErpSupplier byId = supplierService.getById(scmSupplier.getId());
        if (byId == null) return AjaxResult.error("供应商数据不存在");
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return AjaxResult.error("无权限操作");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            // 商户
            if (scmSupplier.getMerchantId() != SecurityUtils.getDeptId() && scmSupplier.getShopId() != 0) {
                return AjaxResult.error("无权限操作");
            }
        } else {
            return AjaxResult.error("无权限操作");
        }
        scmSupplier.setUpdateTime(new Date());
        return toAjax(supplierService.updateById(scmSupplier));
    }

    /**
     * 删除【请填写功能名称】
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        if (ids == null || ids.length == 0) return AjaxResult.error("缺少参数id");
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return AjaxResult.error("未登录用户");
        } else if (userIdentity != EnumUserType.MERCHANT.getIndex()) {
            return AjaxResult.error("无权限操作");
        }
        for (Long id : ids) {
            ErpSupplier byId = supplierService.getById(id);
            if (byId == null) return AjaxResult.error("供应商数据不存在");
            if (userIdentity == 0) {
                if (byId.getMerchantId() != 0) return AjaxResult.error("无权限操作");
            } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
                // 商户
                if (byId.getMerchantId() != SecurityUtils.getDeptId() && byId.getShopId() != 0) {
                    return AjaxResult.error("无权限操作");
                }
            } else if (userIdentity == EnumUserType.STORE.getIndex()) {
                // 店铺
                if (byId.getShopId() != SecurityUtils.getDeptId()) {
                    return AjaxResult.error("无权限操作");
                }
            } else {
                return AjaxResult.error("无权限操作");
            }
            supplierService.removeById(id);
        }
        return AjaxResult.success();
    }
}
