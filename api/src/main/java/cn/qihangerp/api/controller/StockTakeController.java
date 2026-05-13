package cn.qihangerp.api.controller;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpWarehouseStockTake;
import cn.qihangerp.model.request.WarehouseStockTakeAddItemRequest;
import cn.qihangerp.model.request.WarehouseStockTakeCreateRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.security.common.SecurityUtils;
import cn.qihangerp.service.ErpWarehouseStockTakeService;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/stockTake")
public class StockTakeController extends BaseController {
    private final ErpWarehouseStockTakeService stockTakeService;

    @GetMapping("/list")
    public TableDataInfo list(ErpWarehouseStockTake bo, PageQuery pageQuery, HttpServletRequest request)
    {

        var pageList = stockTakeService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }

    @PostMapping("/create")
    public AjaxResult createEntry(@RequestBody WarehouseStockTakeCreateRequest param, HttpServletRequest request)
    {
        if(param.getWarehouseId()==null||param.getWarehouseId()==0) return AjaxResult.error("缺少参数：warehouseId");

        param.setUserName(SecurityUtils.getLoginUser().getUsername());
        param.setUserId(SecurityUtils.getLoginUser().getUserId());
        ResultVo<Long> resultVo = stockTakeService.createEntry(SecurityUtils.getLoginUser().getUserId(), SecurityUtils.getLoginUser().getUsername(), param);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }


    @PostMapping("/addItem")
    public AjaxResult addItem(@RequestBody WarehouseStockTakeAddItemRequest param, HttpServletRequest request)
    {
        log.info("===添加盘点item：{}", JSONObject.toJSONString(param));
        ResultVo resultVo = stockTakeService.addItem(SecurityUtils.getLoginUser().getUserId(), SecurityUtils.getLoginUser().getUsername(), param);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        ErpWarehouseStockTake entry = stockTakeService.getDetailAndItemById(id);
        return success(entry);
    }

    @PostMapping(value = "/complete/{id}")
    public AjaxResult stockTakeComplete(@PathVariable("id") Long id)
    {
        var resultVo = stockTakeService.complete(id,SecurityUtils.getLoginUser().getUserId(), SecurityUtils.getLoginUser().getUsername());
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

}
