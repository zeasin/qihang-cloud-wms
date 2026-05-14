package cn.qihangerp.api.controller.goods;

import cn.qihangerp.common.*;
import cn.qihangerp.common.enums.EnumUserType;
import cn.qihangerp.model.bo.GoodsAddBo;
import cn.qihangerp.model.bo.GoodsSkuNewAddBo;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.query.GoodsQuery;
import cn.qihangerp.model.query.GoodsSkuQuery;
import cn.qihangerp.model.vo.GoodsSpecListVo;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.*;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.qihangerp.common.AjaxResult.CODE_TAG;
import static cn.qihangerp.common.AjaxResult.MSG_TAG;

/**
 * 商品管理Controller
 * 
 * @author qihang
 * @date 2023-12-29
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/erp-api/goods")
public class GoodsController extends BaseController
{
    private final OGoodsService goodsService;
    private final OShopService shopService;
    private final OGoodsCategoryService goodsCategoryService;
    private final OGoodsBrandService goodsBrandService;
    private final OGoodsSkuService skuService;
    private final ErpMerchantService merchantService;
    /**
     * 查询商品列表
     */
    @GetMapping("/list")
    public TableDataInfo list(GoodsQuery goods, PageQuery pageQuery) {
        log.info("======商品库list======{}", JSON.toJSONString(goods));
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            return getDataTable(new  ArrayList<>());
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            // 查所有
            PageResult<OGoods> pageList = goodsService.queryMerchantPageList(SecurityUtils.getDeptId(),goods, pageQuery);
            return getDataTable(pageList);

        }else{
            log.error("无权限");
            return getDataTable(new  ArrayList<>());
        }


    }

    /**
     * 商品skulist
     * @param bo
     * @param pageQuery
     * @return
     */
    @GetMapping("/sku_list")
    public TableDataInfo skuList(GoodsSkuQuery bo, PageQuery pageQuery) {
        log.info("======商品库list======{}", JSON.toJSONString(bo));
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return getDataTable(new ArrayList<>());
        } else if (userIdentity == EnumUserType.MERCHANT.getIndex()) {
            //
            var pageList = goodsService.querySkuMerchantPageList(SecurityUtils.getDeptId(), bo, pageQuery);
            return getDataTable(pageList);
        } else {
            log.error("无权限");
            return getDataTable(new ArrayList<>());
        }
    }

    /**
     * 搜索商品SKU
     * 条件：商品编码、SKU、商品名称
     */
    @GetMapping("/searchSku")
    public TableDataInfo searchSkuBy(String keyword)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        Long merchantId = 0l;
        if (userIdentity == null || userIdentity == 0) {
            // 查自营merchaid 0是为了兼容有方的老版本
            ErpMerchant merchant = merchantService.getById(0);
            if (merchant != null) {
                merchantId = merchant.getId();
            }else{
                merchant = merchantService.getById(1L);
                if (merchant != null) {
                    merchantId = merchant.getId();
                }
            }
        } else if (userIdentity == 20) {
            merchantId = SecurityUtils.getDeptId();
        }else{
            merchantId = -1L;
        }

        List<GoodsSpecListVo> list = goodsService.searchGoodsSpec(merchantId,keyword);
        return getDataTable(list);
    }

    /**
     * 获取商品详细
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(goodsService.selectGoodsById(id));
    }
    /**
     * 获取商品管理详细信息
     */
    @GetMapping(value = "/sku/{id}")
    public AjaxResult getSkuInfo(@PathVariable("id") Long id)
    {
        return success(skuService.getById(id));
    }
    /**
     * 新增商品管理
     */
    @PostMapping("/add")
    public AjaxResult add(@RequestBody GoodsAddBo goods)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if (userIdentity == null) {
            // 登录用户错误
            return AjaxResult.error("登录错误，请重新登录");
        } else if (userIdentity != EnumUserType.MERCHANT.getIndex()) {
            return AjaxResult.error("无权限");
        }
        goods.setMerchantId(SecurityUtils.getDeptId());
        ResultVo<Long> resultVo = goodsService.insertGoods(getUsername(), goods);
        if(resultVo.getCode()!=0) return AjaxResult.error(resultVo.getMsg());
        else return AjaxResult.success(resultVo.getData());
//        goods.setCreateBy(getUsername());
//        int result = goodsService.insertGoods(goods);
//        if(result == -1) new AjaxResult(501,"商品编码已存在");
//        return toAjax(1);
    }




    /**
     * 修改商品
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OGoods goods) {
//        Integer userIdentity = SecurityUtils.getUserIdentity();
//        if(userIdentity == null||userIdentity==0){
//            if(goods.getMerchantId()==null){
//                goods.setMerchantId(0L);
//            }
//        }else if(userIdentity==20){
//            // 商户 不能变更商品的商户ID
//            goods.setMerchantId(null);
//        }else{
//            return AjaxResult.error("无权限操作");
//        }
        // 不允许修改商户
        goods.setMerchantId(null);
        ResultVo resultVo = goodsService.updateGoods(goods);
        if(resultVo.getCode()==0) return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    /**
     * 添加商品sku
     * @param goods
     * @return
     */
    @PostMapping("/addSku")
    public AjaxResult addSku(@RequestBody GoodsSkuNewAddBo goods)
    {
        if(goods.getId()==null||goods.getId()<=0) return AjaxResult.error("缺少参数ID");
        ResultVo<Long> resultVo = goodsService.insertGoodsSku(getUsername(), goods);
        if(resultVo.getCode()!=0) return AjaxResult.error(resultVo.getMsg());
        else return AjaxResult.success(resultVo.getData());
    }

    /**
     * 修改商品sku
     * @param sku
     * @return
     */
    @PutMapping("/sku")
    public AjaxResult editSku(@RequestBody OGoodsSku sku)
    {
        Integer userIdentity = SecurityUtils.getUserIdentity();
        if(userIdentity == null||userIdentity==0){
            if(sku.getMerchantId()==null){
                sku.setMerchantId(0L);
            }
        }else if(userIdentity==20){
            // 商户 不能变更商品的商户ID
            sku.setMerchantId(null);
        }else{
            return AjaxResult.error("无权限操作");
        }
        ResultVo resultVo = skuService.updateSku(sku);
        if(resultVo.getCode()==0) return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/del/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        int result = goodsService.deleteGoodsByIds(ids);
        if(result==0) return AjaxResult.success();
        else if (result==-100) return AjaxResult.error("有关联的订单，不能删除！");
        else return AjaxResult.error();
    }
    @DeleteMapping("/goodsSkuDel/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        ResultVo result = skuService.deleteSkuById(id);
//        int result = goodsService.deleteGoodsByIds(ids);
        if(result.getCode()==0) return AjaxResult.success();
        else return AjaxResult.error(result.getMsg());
    }

}
