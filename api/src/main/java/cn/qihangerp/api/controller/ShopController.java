package cn.qihangerp.api.controller;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.common.enums.EnumUserType;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.OLogisticsCompanyService;
import cn.qihangerp.service.OShopService;
import cn.qihangerp.security.common.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 店铺Controller
 * 
 * @author qihang
 * @date 2023-12-31
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/oms-api/shop")
public class ShopController extends BaseController {
    private final OLogisticsCompanyService logisticsCompanyService;
    private final OShopService shopService;


    /**
     * 查询店铺列表
     */
    @PreAuthorize("@ss.hasPermi('shop:shop:list')")
    @GetMapping("/list")
    public TableDataInfo list(OShop shop) {
        List<OShop> list = shopService.selectShopList(shop);
        return getDataTable(list);
    }

    /**
     * 分页
     * @param shop
     * @param pageQuery
     * @return
     */
    @GetMapping("/pageList")
    public TableDataInfo pageList(OShop shop, PageQuery pageQuery) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = null;
        if (userIdentity == null) {
            return getDataTable(new ArrayList<>());
        } else if (userIdentity == 0) {
            merchantId = shop.getMerchantId();
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            merchantId = SecurityUtils.getDeptId();
            shop.setMerchantId(merchantId);
        } else if (userIdentity == EnumUserType.WAREHOUSE.getIndex()) {
            return getDataTable(new ArrayList<>());
        } else {
            return getDataTable(new ArrayList<>());
        }

        PageResult<OShop> pageList = shopService.queryPageList(shop, pageQuery);

        return getDataTable(pageList);
    }
    /**
     * 获取店铺详细信息
     */
    @PreAuthorize("@ss.hasPermi('shop:shop:query')")
    @GetMapping(value = "/shop/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(shopService.selectShopById(id));
    }

    /**
     * 新增店铺
     */
    @PreAuthorize("@ss.hasPermi('shop:shop:add')")
    @PostMapping("/shop")
    public AjaxResult add(@RequestBody OShop shop) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            return AjaxResult.error("没有权限");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            return toAjax(shopService.insertShop(shop));
        }else return AjaxResult.error("没有权限");
    }

    /**
     * 修改店铺
     */
    @PreAuthorize("@ss.hasPermi('shop:shop:edit')")
    @PutMapping("/shop")
    public AjaxResult edit(@RequestBody OShop shop) {
//        if(shop.getId()==null) return AjaxResult.error("缺少参数：id");
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            return AjaxResult.error("没有权限");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            shopService.updateShopById(shop);
            return AjaxResult.success();
        }else return AjaxResult.error("没有权限");

    }

    /**
     * 删除店铺
     */
    @PreAuthorize("@ss.hasPermi('shop:shop:remove')")
    @DeleteMapping("/shop/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            return AjaxResult.error("没有权限");
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            return toAjax(shopService.deleteShopByIds(ids));
        }else return AjaxResult.error("没有权限");

    }
}
