package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.bo.LinkErpGoodsSkuBo;
import cn.qihangerp.model.entity.OmsShopGoodsSku;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 1
* @description 针对表【oms_shop_goods_sku(其他渠道店铺商品SKU)】的数据库操作Service
* @createDate 2026-01-02 09:22:54
*/
public interface OmsShopGoodsSkuService extends IService<OmsShopGoodsSku> {
    PageResult<OmsShopGoodsSku> queryPageList(OmsShopGoodsSku bo, PageQuery pageQuery);
    ResultVo<Integer> saveGoods(OmsShopGoodsSku goods);
    ResultVo linkErpGoodsSku(LinkErpGoodsSkuBo bo);
}
