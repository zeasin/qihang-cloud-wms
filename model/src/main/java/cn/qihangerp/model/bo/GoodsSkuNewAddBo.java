package cn.qihangerp.model.bo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 商品管理对象 erp_goods
 *
 * @author qihang
 * @date 2023-12-29
 */
@Data
public class GoodsSkuNewAddBo
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    private String[] colorValues;
    private Map<Long,String> colorImages;
//    private Map<Long,String> colorNames;
    private String[] sizeValues;
    private String[] styleValues;
    private List<GoodsAddSkuBo> specList;

}
