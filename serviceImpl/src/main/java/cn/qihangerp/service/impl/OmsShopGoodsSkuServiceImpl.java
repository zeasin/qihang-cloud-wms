package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.bo.LinkErpGoodsSkuBo;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.mapper.OGoodsMapper;
import cn.qihangerp.mapper.OGoodsSkuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.service.OmsShopGoodsSkuService;
import cn.qihangerp.mapper.OmsShopGoodsSkuMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
* @author 1
* @description 针对表【oms_shop_goods_sku(其他渠道店铺商品SKU)】的数据库操作Service实现
* @createDate 2026-01-02 09:22:53
*/
@AllArgsConstructor
@Service
public class OmsShopGoodsSkuServiceImpl extends ServiceImpl<OmsShopGoodsSkuMapper, OmsShopGoodsSku>
    implements OmsShopGoodsSkuService{
    private final OGoodsSkuMapper goodsSkuMapper;
    private final OGoodsMapper goodsMapper;
    @Override
    public PageResult<OmsShopGoodsSku> queryPageList(OmsShopGoodsSku bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OmsShopGoodsSku> queryWrapper = new LambdaQueryWrapper<OmsShopGoodsSku>()
                .eq(bo.getShopId()!=null,OmsShopGoodsSku::getShopId,bo.getShopId())
                .eq(bo.getShopType()!=null,OmsShopGoodsSku::getShopType,bo.getShopType())
                .eq(StringUtils.hasText(bo.getProductId()),OmsShopGoodsSku::getProductId,bo.getProductId())
                .eq(StringUtils.hasText(bo.getSkuId()),OmsShopGoodsSku::getSkuId,bo.getSkuId())
                .like(StringUtils.hasText(bo.getProductTitle()),OmsShopGoodsSku::getProductTitle,bo.getProductTitle())
                .like(StringUtils.hasText(bo.getSkuName()),OmsShopGoodsSku::getSkuName,bo.getSkuName())
                .eq(bo.getErpGoodsSkuId()!=null,OmsShopGoodsSku::getErpGoodsSkuId,bo.getErpGoodsSkuId())
                .eq(bo.getStatus()!=null,OmsShopGoodsSku::getStatus,bo.getStatus())
                .eq(bo.getErpStatus()!=null,OmsShopGoodsSku::getErpStatus,bo.getErpStatus())
                ;

        Page<OmsShopGoodsSku> goodsPage = this.baseMapper.selectPage(pageQuery.build(), queryWrapper);
        return PageResult.build(goodsPage);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Integer> saveGoods(OmsShopGoodsSku goods) {
        List<OmsShopGoodsSku> skus = this.baseMapper.selectList(new LambdaQueryWrapper<OmsShopGoodsSku>()
                .eq(OmsShopGoodsSku::getShopId, goods.getShopId())
                .eq(OmsShopGoodsSku::getSkuId, goods.getSkuId()));
        if(skus.isEmpty()){
            //新增
            goods.setCreateOn(new Date());
            this.baseMapper.insert(goods);
        }else{
            goods.setUpdateOn(new Date());
            this.baseMapper.updateById(goods);
        }
        return ResultVo.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo linkErpGoodsSku(LinkErpGoodsSkuBo bo) {
        OGoodsSku oGoodsSku = goodsSkuMapper.selectById(bo.getErpGoodsSkuId());
        if(oGoodsSku == null) return ResultVo.error("未找到系统商品sku");

        OGoods oGoods=goodsMapper.selectById(oGoodsSku.getGoodsId());
        if(oGoods == null){
            return ResultVo.error("未找到系统商品");
        }

        OmsShopGoodsSku goodsSku = this.baseMapper.selectById(bo.getId());
        if(goodsSku == null) {
            return ResultVo.error("店铺商品sku数据不存在");
        }


        OmsShopGoodsSku sku = new OmsShopGoodsSku();
        sku.setId(Long.parseLong(bo.getId()));
        sku.setErpGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
        sku.setErpGoodsSkuId(Long.parseLong(oGoodsSku.getId()));
        sku.setErpStatus(1);
        sku.setUpdateOn(new Date());
        this.baseMapper.updateById(sku);

        return ResultVo.success();
    }
}




