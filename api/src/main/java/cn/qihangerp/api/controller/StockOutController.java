package cn.qihangerp.api.controller;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpStockOut;
import cn.qihangerp.model.entity.ErpStockOutItem;
import cn.qihangerp.model.entity.ErpStockOutItemDetail;
import cn.qihangerp.model.request.StockOutCreateRequest;
import cn.qihangerp.model.request.StockOutItemRequest;
import cn.qihangerp.service.ErpStockOutItemDetailService;
import cn.qihangerp.service.ErpStockOutItemService;
import cn.qihangerp.service.ErpStockOutService;
import cn.qihangerp.security.common.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/stockOut")
public class StockOutController extends BaseController {
    private final ErpStockOutService stockOutService;
    private final ErpStockOutItemService stockOutItemService;
    private final ErpStockOutItemDetailService stockOutItemDetailService;

    @GetMapping("/list")
    public TableDataInfo list(ErpStockOut bo, PageQuery pageQuery)
    {
        var pageList = stockOutService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/item_list")
    public TableDataInfo itemList(ErpStockOutItem bo, PageQuery pageQuery)
    {
        var pageList = stockOutItemService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }
    @GetMapping("/detail_list")
    public TableDataInfo detailList(ErpStockOutItemDetail bo, PageQuery pageQuery)
    {
        var pageList = stockOutItemDetailService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }


    @PostMapping("/create")
    public AjaxResult createEntry(@RequestBody StockOutCreateRequest request)
    {
        ResultVo<Long> resultVo = stockOutService.createEntry(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        ErpStockOut entry = stockOutService.getDetailAndItemById(id);
        return success(entry);
    }
    @GetMapping(value = "/item/{id}")
    public AjaxResult itemDetail(@PathVariable("id") Long id)
    {
        ErpStockOutItem entry = stockOutItemService.getDetailAndInventoryBatchById(id);
        return success(entry);
    }

    @PostMapping("/out")
    public AjaxResult out(@RequestBody StockOutItemRequest request)
    {
        ResultVo<Long> resultVo = stockOutService.stockOut(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }
}
