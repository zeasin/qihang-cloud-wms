package cn.qihangerp.api.controller;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpWarehouse;
import cn.qihangerp.model.entity.ErpWarehousePosition;
import cn.qihangerp.service.ErpWarehousePositionService;
import cn.qihangerp.service.ErpWarehouseService;
import cn.qihangerp.security.common.BaseController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/warehouse")
public class WarehouseController extends BaseController {
    private final ErpWarehouseService warehouseService;
    private final ErpWarehousePositionService positionService;
    @GetMapping("/list")
    public TableDataInfo list(ErpWarehouse bo)
    {
        LambdaQueryWrapper<ErpWarehouse> qw = new LambdaQueryWrapper<ErpWarehouse>()
                .eq(bo.getStatus()!=null, ErpWarehouse::getStatus, bo.getStatus())
                .like(StringUtils.hasText(bo.getNumber()), ErpWarehouse::getNumber,bo.getNumber())
                .like(StringUtils.hasText(bo.getName()), ErpWarehouse::getName,bo.getName())
                ;
        List<ErpWarehouse> erpWarehouses = warehouseService.list(qw);
        return getDataTable(erpWarehouses);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(warehouseService.getById(id));
    }
    @PostMapping
    public AjaxResult add(@RequestBody ErpWarehouse warehouse)
    {
        warehouse.setCreateBy(getUsername());
        warehouse.setCreateTime(new Date());
        boolean save = warehouseService.save(warehouse);
        if(save){
            ErpWarehousePosition position = new ErpWarehousePosition();
            position.setWarehouseId(warehouse.getId());
            position.setParentId(0);
            position.setParentId1(0);
            position.setParentId2(0);
            position.setNumber(warehouse.getNumber());
            position.setName(warehouse.getName());
            position.setIsDelete(0);
            position.setAddress(warehouse.getAddress());
            position.setRemark(warehouse.getRemark());
            position.setCreateBy(getUsername());
            position.setCreateTime(new Date());
            positionService.save(position);
        }
        return AjaxResult.success();
    }
    @PutMapping
    public AjaxResult edit(@RequestBody ErpWarehouse warehouse)
    {
        warehouse.setUpdateBy(getUsername());
        warehouse.setUpdateTime(new Date());
        return toAjax(warehouseService.updateById(warehouse));
    }
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(warehouseService.removeBatchByIds(Arrays.stream(ids).toList()));
    }

    @GetMapping("/position/list")
    public TableDataInfo positionList(Long warehouseId)
    {
        LambdaQueryWrapper<ErpWarehousePosition> qw = new LambdaQueryWrapper<ErpWarehousePosition>()
                .eq(ErpWarehousePosition::getWarehouseId,warehouseId)
                ;
        List<ErpWarehousePosition> list = positionService.list(qw);
        return getDataTable(list);
    }
    @GetMapping("/position/search")
    public TableDataInfo searchPosition(Long warehouseId,String number)
    {
        LambdaQueryWrapper<ErpWarehousePosition> qw = new LambdaQueryWrapper<ErpWarehousePosition>()
                .eq(ErpWarehousePosition::getWarehouseId,warehouseId)
                .like(ErpWarehousePosition::getNumber,number)
                ;
        List<ErpWarehousePosition> list = positionService.list(qw);
        return getDataTable(list);
    }


    @PostMapping("/position")
    public AjaxResult positionAdd(@RequestBody ErpWarehousePosition position) {
        position.setCreateBy(getUsername());
        position.setCreateTime(new Date());
        position.setParentId1(0);
        position.setParentId2(0);
        positionService.save(position);

        return AjaxResult.success();
    }

    @GetMapping(value = "/position/{id}")
    public AjaxResult getPositionInfo(@PathVariable("id") Long id)
    {
        return success(positionService.getById(id));
    }

    @PutMapping("/position")
    public AjaxResult positionEdit(@RequestBody ErpWarehousePosition position)
    {
        position.setUpdateBy(getUsername());
        position.setUpdateTime(new Date());
        return toAjax(positionService.updateById(position));
    }
    @DeleteMapping("/position/{ids}")
    public AjaxResult positionRemove(@PathVariable Long[] ids)
    {
        return toAjax(positionService.removeBatchByIds(Arrays.stream(ids).toList()));
    }


}
