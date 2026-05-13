package cn.qihangerp.api.controller.oms;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.csv.CSVReaderWithApacheCommons;
import cn.qihangerp.common.enums.EnumShopType;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.model.request.OrderImportRequest;
import cn.qihangerp.model.vo.OrderItemImportVo;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.OShopService;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RequestMapping("/shop/order")
@AllArgsConstructor
@RestController
public class ShopOrderImportController {
    private final OShopService shopService;
    private final OOrderService orderService;

    /**
     * 订单导入
     * @param file
     * @param shopId
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    @RequestMapping(value = "/order_read", method = RequestMethod.POST)
    public AjaxResult orderRead(@RequestParam("file") MultipartFile file,
                                  @RequestParam("shopId") Long shopId) throws IOException, InvalidFormatException {
        log.info("========导入店铺订单：{}", shopId);
        if (shopId == null) {
            return AjaxResult.error("缺少参数：shopId");
        }
        OShop shop = shopService.getById(shopId);
        if (shop == null) {
            return AjaxResult.error("店铺不存在");
        }

        String fileName = file.getOriginalFilename();
        String dir = System.getProperty("user.dir");
        String uploadDir = dir + File.separator + "import_order";
        String destFileName = uploadDir + "/order_" + shopId + "_" + fileName;
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("上传目录创建成功: " + uploadDir);
        } else {
            if (!Files.isDirectory(uploadPath)) {
                throw new RuntimeException("上传路径已存在但不是目录: " + uploadDir);
            }
            System.out.println("上传目录已存在: " + uploadDir);
        }

        System.out.println(destFileName);
        File destFile = new File(destFileName);
        file.transferTo(destFile);
//        File csvFile = new File(destFileName);

        List<OrderItemImportVo> orderItemList = new ArrayList<>();
        if (shop.getType().intValue() == EnumShopType.PDD.getIndex()) {
            // 读取CSV
            List<LinkedHashMap<String, String>> data = CSVReaderWithApacheCommons.readCSVToMap(destFile);
            // 组合数据
            for (LinkedHashMap<String, String> row : data) {
                OrderItemImportVo orderItem = new OrderItemImportVo();
                Map.Entry<String, String> firstEntry = row.entrySet().iterator().next();
                orderItem.setGoodsTitle(firstEntry.getValue());
                String orderSn = row.get("订单号");
                String skuId = row.get("样式ID");
                orderItem.setOrderNum(orderSn);
                orderItem.setSubOrderNum(orderSn+"-"+skuId);
                orderItem.setOrderStatusText(row.get("订单状态"));
                orderItem.setRefundStatusText(row.get("售后状态"));
                orderItem.setOrderTime(row.get("订单成交时间"));

                orderItem.setGoodsId(row.get("商品id"));
                orderItem.setSkuId(skuId);
                orderItem.setSkuNum(row.get("商家编码-规格维度"));
                orderItem.setGoodsSpec(row.get("商品规格"));
                orderItem.setGoodsNum(row.get("商家编码-商品维度"));

                orderItem.setGoodsAmount(Double.parseDouble(row.get("商品总价(元)")));
                orderItem.setPostAmount(Double.parseDouble(row.get("邮费(元)")));
                orderItem.setSellerDiscount(Double.parseDouble(row.get("店铺优惠折扣(元)")));
                orderItem.setPlatformDiscount(Double.parseDouble(row.get("平台优惠折扣(元)")));
                orderItem.setPaymentDiscount(Double.parseDouble(row.get("多多支付立减金额(元)")));
                orderItem.setPayment(Double.parseDouble(row.get("用户实付金额(元)")));
                orderItem.setSellerAmount(Double.parseDouble(row.get("商家实收金额(元)")));
                orderItem.setRemark(row.get("商家备注"));

                orderItem.setQuantity(Integer.parseInt(row.get("商品数量(件)")));

                orderItem.setShipTime(row.get("发货时间"));
                orderItem.setDeliveryTime(row.get("确认收货时间"));
                orderItem.setExpressCompany(row.get("快递公司"));
                orderItem.setExpressCode(row.get("快递单号"));
                orderItemList.add(orderItem);
            }
            return AjaxResult.success(orderItemList);
        } else {
            InputStream fis = null;
            fis = new FileInputStream(destFileName);
            if (fis == null) return AjaxResult.error("没有文件");

            Workbook workbook = null;

            try {
                if (fileName.toLowerCase().endsWith("xlsx")) {
                    workbook = new XSSFWorkbook(fis);
                } else if (fileName.toLowerCase().endsWith("xls")) {
                    workbook = new HSSFWorkbook(fis);
                }
                // workbook = new HSSFWorkbook(fis);
            } catch (Exception ex) {
                return AjaxResult.error(ex.getMessage());
            }

            if (workbook == null) return AjaxResult.error(502, "未读取到Excel文件");


            /****************开始处理excel****************/
            int success = 0;
            int fail = 0;
            Sheet sheet = null;
            try {
                sheet = workbook.getSheetAt(0);
                int lastRowNum = sheet.getLastRowNum();//最后一行索引
                Row row = null;
                if (shop.getType().intValue() == EnumShopType.PDD.getIndex()) {
                    for (int i = 1; i <= lastRowNum; i++) {
                        row = sheet.getRow(i);
                        //数据
                        OrderItemImportVo orderItem = new OrderItemImportVo();

                        String orderSn = row.getCell(1).getStringCellValue().replace("\t", "");
                        String goodsName = row.getCell(0).getStringCellValue().replace("\t", "");
                        String statusText = row.getCell(2).getStringCellValue().replace("\t", "");
                        Cell goodsPriceCell = row.getCell(3);
                        String goodsPriceValue = "0.0";

                        if (goodsPriceCell != null) {
                            if (goodsPriceCell.getCellType() == CellType.STRING) {
                                goodsPriceValue = goodsPriceCell.getStringCellValue().replace("\t", "");
                            } else if (goodsPriceCell.getCellType() == CellType.NUMERIC) {
                                goodsPriceValue = NumberToTextConverter.toText(goodsPriceCell.getNumericCellValue()).replace("\t", "");
                            }
                        }
                        orderItem.setOrderNum(orderSn);
                        orderItem.setGoodsTitle(goodsName);
                        orderItem.setOrderStatusText(statusText);
                        orderItem.setItemAmount(Double.valueOf(goodsPriceValue));
                        orderItemList.add(orderItem);
                        success++;
                    }
                }

            } catch (Exception ex) {
                fail++;
                ex.printStackTrace();
            }
            return AjaxResult.error("暂时不支持");
        }

    }


    @RequestMapping(value = "/order_import", method = RequestMethod.POST)
    public AjaxResult orderImport(@RequestBody OrderImportRequest request) {
        log.info("========订单导入：{}",JSON.toJSONString(request));
        ResultVo<Long> resultVo = orderService.importOrder(request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }
}
