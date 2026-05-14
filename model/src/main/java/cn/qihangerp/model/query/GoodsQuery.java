package cn.qihangerp.model.query;

import lombok.Data;

@Data
public class GoodsQuery {
    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品编号
     */
    private String goodsNum;
    /**
     * 商品唯一ID
     */
    private String outerErpGoodsId;
    private String sellerId;//卖家ID(外部系统使用)
    private String sellerBrandId;//卖家品牌ID(外部系统使用)

    /**
     * 计价方式：0一口价；1金包银+工费；
     */
    private Integer priceType;

    /**
     * 库存模式：0-传统SKU模式，1-一物一码模式（珠宝）
     */
    private Integer inventoryMode;
    /**
     * 供应商id
     */
    private Long supplierId;
    /**
     * 商品分类ID
     */
    private Long categoryId;
    /**
     * 品牌id
     */
    private Long brandId;
    private Long merchantId;
    private Long ownerId;

    /**
     * 状态
     */
    private Integer status;
}
