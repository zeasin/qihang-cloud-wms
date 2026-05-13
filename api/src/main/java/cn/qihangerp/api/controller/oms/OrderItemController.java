package cn.qihangerp.api.controller.oms;



import cn.qihangerp.api.request.OrderItemUpdateSkuRequest;
import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.bo.OrderItemListBo;
import cn.qihangerp.service.OOrderItemService;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.security.common.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺订单Controller
 *
 * @author qihang
 * @date 2023-12-31
 */
@AllArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderItemController extends BaseController
{
    private final OOrderItemService itemService;
    private final OOrderService orderService;

    /**
     * 查询店铺订单列表
     */
    @GetMapping("/item_list")
    public TableDataInfo list(OrderItemListBo bo, PageQuery pageQuery)
    {
        var pageList = itemService.selectPageVo(pageQuery,bo);
        return getDataTable(pageList);
    }

    /**
     * 更新erp sku id
     * @param request
     * @return
     */
    @PostMapping("/updateErpSkuId")
    public AjaxResult ship(@RequestBody OrderItemUpdateSkuRequest request)
    {
        if(request.getId()==null||request.getId()==0) return AjaxResult.error("缺少参数：id");
        if(StringUtils.isEmpty(request.getErpSkuId()) || request.getErpSkuId().equals("0")){
            return AjaxResult.error("确少参数:ErpSkuId");
        }
        var result = itemService.updateErpSkuId(request.getId(), request.getErpSkuId());
        if(result.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(result.getMsg());
    }
}
