package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.mapper.OGoodsMapper;
import cn.qihangerp.mapper.OGoodsSkuMapper;
import cn.qihangerp.mapper.ShopGoodsMapper;
import cn.qihangerp.mapper.ShopGoodsSkuMapper;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.request.ShopGoodsAddRequest;
import cn.qihangerp.model.request.ShopGoodsSkuAddRequest;
import cn.qihangerp.model.request.ShopGoodsSkuInsertRequest;
import cn.qihangerp.model.request.ShopGoodsSkuUpdateRequest;
import cn.qihangerp.service.OGoodsSkuService;
import cn.qihangerp.service.OShopService;
import cn.qihangerp.service.ShopGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author qilip
* @description 针对表【oms_shop_goods(其他渠道店铺商品)】的数据库操作Service实现
* @createDate 2025-07-15 08:29:21
*/
@Slf4j
@AllArgsConstructor
@Service
public class ShopGoodsServiceImpl extends ServiceImpl<ShopGoodsMapper, ShopGoods>
    implements ShopGoodsService {
    private final OGoodsSkuMapper goodsSkuMapper;
    private final OGoodsMapper goodsMapper;
    private final OGoodsSkuService goodsSkuService;
    private final ShopGoodsSkuMapper shopGoodsSkuMapper;
//    private final ShopGoodsSkuMappingService shopGoodsSkuMappingService;
    private final OShopService shopService;

    @Override
    public PageResult<ShopGoods> queryPageList(ShopGoods bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ShopGoods> queryWrapper = new LambdaQueryWrapper<ShopGoods>()
//                .eq(ShopGoods::getDeliverMethod,0)
                .eq(bo.getId()!=null,ShopGoods::getId,bo.getId())
                .eq(StringUtils.hasText(bo.getProductId()),ShopGoods::getProductId,bo.getProductId())
                .eq(StringUtils.hasText(bo.getSpuCode()),ShopGoods::getSpuCode,bo.getSpuCode())
                .eq(StringUtils.hasText(bo.getOuterProductId()),ShopGoods::getOuterProductId,bo.getOuterProductId())
                .like(StringUtils.hasText(bo.getTitle()),ShopGoods::getTitle,bo.getTitle())
                .eq(bo.getShopId()!=null,ShopGoods::getShopId,bo.getShopId())
                .eq(bo.getShopType()!=null,ShopGoods::getShopType,bo.getShopType())
                .eq(bo.getMerchantId()!=null,ShopGoods::getMerchantId,bo.getMerchantId())
                ;

        Page<ShopGoods> goodsPage = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        if(goodsPage.getTotal()>0){
            for(ShopGoods goods : goodsPage.getRecords()){
                goods.setSkuList(shopGoodsSkuMapper.selectList(new LambdaQueryWrapper<ShopGoodsSku>().eq(ShopGoodsSku::getShopGoodsId,goods.getId())));
            }
        }
        return PageResult.build(goodsPage);
    }



    /**
     * 手动添加店铺商品
     * @param goodsAddRequest
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> addGoods(ShopGoodsAddRequest goodsAddRequest, OShop shop) {
        if(goodsAddRequest.getSkuList()==null||goodsAddRequest.getSkuList().isEmpty()) return ResultVo.error("没有商品规格信息");
//        OShop shop = shopService.getById(goodsAddRequest.getShopId());
//        if(shop==null) return ResultVo.error("店铺不存在");

        if(StringUtils.hasText(goodsAddRequest.getProductId())&&!goodsAddRequest.getProductId().equals("0")){
            // 有值，判断是否存在
            List<ShopGoods> shopGoods = this.baseMapper.selectList(new LambdaQueryWrapper<ShopGoods>().eq(ShopGoods::getProductId, goodsAddRequest.getProductId()));
            if(!shopGoods.isEmpty()) return ResultVo.error("商品平台ID已存在");
        }
        Long c = System.currentTimeMillis()/1000;
        String erpGoodsId="0";
        // 组合sku
        List<ShopGoodsSku> skuList = new ArrayList<>();
        int total=0;
        for (ShopGoodsAddRequest.sku sku:goodsAddRequest.getSkuList()){
            ShopGoodsSku s = new ShopGoodsSku();
            s.setProductId(goodsAddRequest.getProductId());
            s.setProductTitle(goodsAddRequest.getGoodsName());
            s.setSkuId(sku.getSkuId());
            s.setOuterProductId(goodsAddRequest.getOuterProductId());
            s.setOuterSkuId(sku.getOuterSkuId());
            s.setImg(StringUtils.hasText(sku.getImg())?sku.getImg(): goodsAddRequest.getGoodsImg());
            s.setPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(sku.getPrice())).intValue());
            s.setStockNum(sku.getStockNum());
            s.setSkuCode(sku.getSkuCode());
            s.setSkuName(sku.getSkuName());
            s.setStatus(1);
            s.setAddTime(c);
            s.setStock(sku.getStockNum());
            s.setCreateOn(new Date());
            s.setBindShipSku(0);
            // 查询skucode
            List<OGoodsSku> oGoodsSkus = goodsSkuMapper.selectList(new LambdaQueryWrapper<OGoodsSku>().eq(OGoodsSku::getSkuCode, sku.getSkuCode()));
            if(!oGoodsSkus.isEmpty()){
                erpGoodsId = oGoodsSkus.get(0).getGoodsId();
                s.setErpGoodsSkuId(Long.parseLong(oGoodsSkus.get(0).getId()));
            }
            s.setErpGoodsId(Long.parseLong(erpGoodsId));
            skuList.add(s);
            total+= s.getStockNum();
        }

        ShopGoods goods = new ShopGoods();
        goods.setShopId(shop.getId());
        goods.setShopType(shop.getType());
        goods.setMerchantId(shop.getMerchantId());
        goods.setProductId(goodsAddRequest.getProductId());
        goods.setOuterProductId(goodsAddRequest.getOuterProductId());
        goods.setTitle(goodsAddRequest.getGoodsName());
        goods.setImg(goodsAddRequest.getGoodsImg());
        goods.setStatus(1);
        goods.setMinPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(goodsAddRequest.getMinPrice())).intValue());
        goods.setSpuCode(goodsAddRequest.getGoodsNum());
        goods.setQuantity(total);
        goods.setErpGoodsId(Long.parseLong(erpGoodsId));
        goods.setAddTime(c);
        goods.setCreateOn(new Date());
        goods.setDeliverMethod(0);
        goods.setBindShipSku(0);
        this.baseMapper.insert(goods);
        //插入sku
        for(var sku:skuList){
            sku.setShopGoodsId(goods.getId());
            sku.setShopId(goods.getShopId());
            sku.setShopType(goods.getShopType());
            sku.setMerchantId(goods.getMerchantId());
            shopGoodsSkuMapper.insert(sku);
        }
        return ResultVo.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> addGoodsSku(ShopGoodsSkuAddRequest request) {
        OGoodsSku oGoodsSku = goodsSkuMapper.selectById(request.getGoodsSkuId());
        if(oGoodsSku==null){
            return ResultVo.error("没有找到商品库sku");
        }
        OShop shop = shopService.getById(request.getShopId());
        if(shop==null) return ResultVo.error("店铺不存在");

        Long shopGoodsId = null;
        Long shopGoodsSkuId = null;

            // 查询商品是否存在
            List<ShopGoods> shopGoods = this.baseMapper.selectList(new LambdaQueryWrapper<ShopGoods>().eq(ShopGoods::getShopId, request.getShopId()).eq(ShopGoods::getErpGoodsId, oGoodsSku.getGoodsId()));
            if (shopGoods.isEmpty()) {
                // 不存在，增加一条商品信息
                ShopGoods goods = new ShopGoods();
                goods.setShopId(shop.getId());
                goods.setShopType(shop.getType());
                goods.setMerchantId(shop.getMerchantId());
                goods.setProductId("");
                goods.setOuterProductId("");
                goods.setTitle(oGoodsSku.getGoodsName());
                goods.setImg(oGoodsSku.getColorImage());
                goods.setStatus(1);
                goods.setMinPrice(BigDecimal.valueOf(100).multiply(oGoodsSku.getRetailPrice()).intValue());
                goods.setSpuCode("");
                goods.setQuantity(0);
                goods.setErpGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
                goods.setAddTime(System.currentTimeMillis() / 100);
                goods.setCreateOn(new Date());
                goods.setDeliverMethod(0);
                goods.setBindShipSku(0);
                this.baseMapper.insert(goods);
                shopGoodsId = goods.getId();
            } else {
                // 存在，不动
                shopGoodsId = shopGoods.get(0).getId();
            }

        // 查询商品sku是否存在
        List<ShopGoodsSku> shopGoodsSkus = shopGoodsSkuMapper.selectList(new LambdaQueryWrapper<ShopGoodsSku>()
                .eq(ShopGoodsSku::getErpGoodsSkuId, request.getGoodsSkuId())
                .eq(StringUtils.hasText(request.getSkuId()), ShopGoodsSku::getSkuId, request.getSkuId())
                .eq(ShopGoodsSku::getShopId, request.getShopId())
        );
        if(shopGoodsSkus.isEmpty()){
            // 不存在 添加
            ShopGoodsSku s = new ShopGoodsSku();
            s.setShopGoodsId(shopGoodsId);
            s.setShopId(shop.getId());
            s.setShopType(shop.getType());
            s.setShopName(shop.getName());
            s.setMerchantId(shop.getMerchantId());
            s.setProductId("");
            s.setProductTitle(oGoodsSku.getGoodsName());
            s.setSkuId(request.getSkuId());
            s.setOuterProductId("");
            s.setOuterSkuId(oGoodsSku.getSkuCode());
            s.setImg( oGoodsSku.getColorImage());
            s.setPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(request.getPrice())).intValue());
            s.setStockNum(0);
            s.setSkuCode(oGoodsSku.getSkuCode());
            s.setSkuName(oGoodsSku.getSkuName());
            s.setStatus(1);
            s.setAddTime(System.currentTimeMillis()/100);
            s.setStock(0);
            s.setCreateOn(new Date());
            s.setBindShipSku(0);
            s.setErpGoodsSkuId(Long.parseLong(oGoodsSku.getId()));
            s.setErpGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
            shopGoodsSkuMapper.insert(s);
            shopGoodsSkuId = s.getId();
        }else{
            // 存在就修改
            shopGoodsSkuId = shopGoodsSkus.get(0).getId();
            ShopGoodsSku s = new ShopGoodsSku();
            s.setId(shopGoodsSkuId);
            s.setSkuId(request.getSkuId());
            s.setPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(request.getPrice())).intValue());
            s.setUpdateOn(new Date());
            s.setModifyTime(System.currentTimeMillis()/100);
            shopGoodsSkuMapper.updateById(s);
        }

        return ResultVo.success(shopGoodsSkuId);
    }

    @Override
    public ResultVo<Long> insertGoodsSku(ShopGoodsSkuInsertRequest request) {
        ShopGoods shopGoods = this.baseMapper.selectById(request.getShopGoodsId());
        if(shopGoods == null) return ResultVo.error("店铺商品不存在");

        OGoodsSku oGoodsSku = goodsSkuMapper.selectById(request.getErpGoodsSkuId());
        if(oGoodsSku==null){
            return ResultVo.error("没有找到商品库sku");
        }
        List<ShopGoodsSku> shopGoodsSkuList = shopGoodsSkuMapper.selectList(new LambdaQueryWrapper<ShopGoodsSku>().eq(ShopGoodsSku::getSkuId, request.getSkuId()));
        if(!shopGoodsSkuList.isEmpty()){
            return ResultVo.error("平台SkuId已经存在");
        }
        // 不存在 添加
        ShopGoodsSku s = new ShopGoodsSku();
        s.setShopGoodsId(request.getShopGoodsId());
        s.setShopId(shopGoods.getShopId());
        s.setShopType(shopGoods.getShopType());
        s.setShopName("");
        s.setMerchantId(shopGoods.getMerchantId());
        s.setProductId(shopGoods.getProductId());
        s.setProductTitle(oGoodsSku.getGoodsName());
        s.setSkuId(request.getSkuId());
        s.setOuterProductId("");
        s.setOuterSkuId(oGoodsSku.getSkuCode());
        s.setImg(oGoodsSku.getColorImage());
        s.setPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(request.getPrice())).intValue());
        s.setStockNum(0);
        s.setSkuCode(oGoodsSku.getSkuCode());
        s.setSkuName(oGoodsSku.getSkuName());
        s.setStatus(1);
        s.setAddTime(System.currentTimeMillis()/100);
        s.setStock(0);
        s.setCreateOn(new Date());
        s.setBindShipSku(0);
        s.setErpGoodsSkuId(Long.parseLong(oGoodsSku.getId()));
        s.setErpGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
        shopGoodsSkuMapper.insert(s);
        return ResultVo.success();
    }

    @Override
    public ResultVo<Long> updateGoodsSku(ShopGoodsSkuUpdateRequest request) {
        ShopGoodsSku shopGoodsSku = shopGoodsSkuMapper.selectById(request.getId());
        if(shopGoodsSku==null) return ResultVo.error("店铺商品Sku不存在");

        OGoodsSku oGoodsSku = goodsSkuMapper.selectById(request.getErpGoodsSkuId());
        if(oGoodsSku==null){
            return ResultVo.error("没有找到商品库sku");
        }

        if(!request.getSkuId().equals(shopGoodsSku.getSkuId())){
            List<ShopGoodsSku> shopGoodsSkuList = shopGoodsSkuMapper.selectList(new LambdaQueryWrapper<ShopGoodsSku>().eq(ShopGoodsSku::getSkuId, request.getSkuId()));
            if(!shopGoodsSkuList.isEmpty()){
                return ResultVo.error("平台SkuId已经存在");
            }
        }

        ShopGoodsSku s = new ShopGoodsSku();
        s.setId(request.getId());
        s.setProductTitle(request.getProductTitle());
        s.setSkuId(request.getSkuId());
        s.setOuterProductId("");
        s.setOuterSkuId(oGoodsSku.getSkuCode());
        s.setErpGoodsSkuId(Long.parseLong(oGoodsSku.getId()));
        s.setErpGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
        s.setImg(request.getImg());
        s.setPrice(BigDecimal.valueOf(100).multiply(BigDecimal.valueOf(request.getPrice())).intValue());
        s.setSkuCode(oGoodsSku.getSkuCode());
        s.setSkuName(request.getSkuName());
        s.setUpdateOn(new Date());
        s.setModifyTime(System.currentTimeMillis()/100);
        shopGoodsSkuMapper.updateById(s);

        return ResultVo.success();
    }

}




