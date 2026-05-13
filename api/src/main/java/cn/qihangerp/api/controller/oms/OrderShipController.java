package cn.qihangerp.api.controller.oms;

import cn.qihangerp.api.request.StockOutEntryGenerateBo;
import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.security.common.BaseController;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/ship")
public class OrderShipController extends BaseController {

    private final OOrderService orderService;


    /**
     * 生成出库单
     */
    @PostMapping("/generate_stock_out_entry")
    public AjaxResult generateStockOutEntry(@RequestBody StockOutEntryGenerateBo bo)
    {
        log.info("============生成出库单========={}", JSON.toJSONString(bo));
        if(bo.getId()==null||bo.getId()==0) return AjaxResult.error("缺少参数：id");
        var result = orderService.generateStockOutEntryByShipOrderId(bo.getId());
        if(result.getCode()==0) return AjaxResult.success();
        else return AjaxResult.error(result.getMsg());
    }



}
