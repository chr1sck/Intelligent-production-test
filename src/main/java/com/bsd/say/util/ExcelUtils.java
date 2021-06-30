//package com.bsd.say.util;
//
//
//import com.oceanspot.template.entities.OrderDetail;
//import com.oceanspot.template.entities.Orders;
//import com.oceanspot.template.vo.OrderVo;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.ss.usermodel.CellType;
//import org.apache.poi.xssf.usermodel.XSSFCellStyle;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.LinkedHashMap;
//import java.util.List;
//
///**
// * excel 工具类
// */
//public class ExcelUtils {
//
//    private static Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
//
//    /**
//     * 通过绝对路径读取excel
//     *
//     * @param absPath
//     */
//    public static List<OrderVo> readExcel(String absPath) {
//        List<OrderVo> orderVoList = new ArrayList<>();
//        ExcelUtils excelUtils = new ExcelUtils();
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
//        SimpleDateFormat simpleDateFormatDetail = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
//        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            InputStream inputStream = new FileInputStream(new File(absPath));
//            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
//            XSSFSheet sheet = workbook.getSheetAt(0);
//
//            for (int i = 2; i < sheet.getLastRowNum() + 2; i++) {
//                Orders orders = new Orders();
//                OrderDetail orderDetail = new OrderDetail();
//                XSSFRow row = sheet.getRow(i);
//                if (row != null) {
//                    OrderVo orderVo = new OrderVo();
//                    //外部单号
//                    if (row.getCell(0) != null) {
//                        row.getCell(0).setCellType(CellType.STRING);
//                        String outOrderNo = row.getCell(0).getStringCellValue();
//                        if (StringUtils.isEmpty(outOrderNo)){
//                            continue;
//                        }
//                        orders.setOutOrderNo(outOrderNo);
//                    }
//                    //订单类型
//                    if (row.getCell(1) != null) {
//                        row.getCell(1).setCellType(CellType.STRING);
//                        String orderType =  row.getCell(1).getStringCellValue();
//                        orders.setOrderType(orderType);
//                    }
//
//                    //货主代码
//                    if (row.getCell(2) != null) {
//                        row.getCell(2).setCellType(CellType.STRING);
//                        String goodsOwnerCode = row.getCell(2).getStringCellValue();
//                        orders.setGoodsOwnerCode(goodsOwnerCode);
//                    }
//                    //承运商代码
//                    if (row.getCell(3) != null) {
//                        row.getCell(3).setCellType(CellType.STRING);
//                        String carriersCode = row.getCell(3).getStringCellValue();
//                        orders.setCarriersCode(carriersCode);
//                    }
//                    //运单号
//                    if (row.getCell(4) != null) {
//                        row.getCell(4).setCellType(CellType.STRING);
//                        String freightNo = row.getCell(4).getStringCellValue();
//                        orders.setFreightNo(freightNo);
//                    }
//                    //仓库代码
//                    if (row.getCell(5) != null) {
//                        row.getCell(5).setCellType(CellType.STRING);
//                        String warehouseCode = row.getCell(5).getStringCellValue();
//                        orders.setWarehouseCode(warehouseCode);
////                        draft.setPrice(bigDecimal);
//                    }
//                    //收货人代码
//                    if (row.getCell(6) != null) {
//                        row.getCell(6).setCellType(CellType.STRING);
//                        String receiverCode = row.getCell(6).getStringCellValue();
//                        orders.setReceiverCode(receiverCode);
//                    }
//                    //收货人名称
//                    if (row.getCell(7) != null) {
//                        row.getCell(7).setCellType(CellType.STRING);
//                        String receiver = row.getCell(7).getStringCellValue();
//                        orders.setReceiver(receiver);
//                    }
//                    //省
//                    if (row.getCell(8) != null) {
//                        row.getCell(8).setCellType(CellType.STRING);
//                        String province = row.getCell(8).getStringCellValue();
//                        orders.setProvince(province);
//                    }
//                    //市
//                    if (row.getCell(9) != null) {
//                        row.getCell(9).setCellType(CellType.STRING);
//                        String city = row.getCell(9).getStringCellValue();
//                        orders.setCity(city);
//                    }
//                    //区
//                    if (row.getCell(10) != null) {
//                        row.getCell(10).setCellType(CellType.STRING);
//                        String district = row.getCell(10).getStringCellValue();
//                        orders.setDistrict(district);
//                    }
//                    //详细地址
//                    if (row.getCell(11) != null) {
//                        row.getCell(11).setCellType(CellType.STRING);
//                        String area = row.getCell(11).getStringCellValue();
//                        orders.setArea(area);
//                    }
//                    //收货人姓名
//                    if (row.getCell(12) != null) {
//                        row.getCell(12).setCellType(CellType.STRING);
//                        String receiverName = row.getCell(12).getStringCellValue();
//                        orders.setReceiverName(receiverName);
//                    }
//                    //收货人电话
//                    if (row.getCell(13) != null) {
//                        row.getCell(13).setCellType(CellType.STRING);
//                        String receiverPhone = row.getCell(13).getStringCellValue();
//                        orders.setReceiverPhone(receiverPhone);
//                    }
//                    //备注
//                    if (row.getCell(14) != null) {
//                        row.getCell(14).setCellType(CellType.STRING);
//                        String remark = row.getCell(14).getStringCellValue();
//                        orders.setRemark(remark);
//                    }
//                    //到城市时效
//                    if (row.getCell(15) != null) {
//                        row.getCell(15).setCellType(CellType.NUMERIC);
//                        Double DoubleCityAging = row.getCell(15).getNumericCellValue();
//                        Integer cityAging = DoubleCityAging.intValue();
//                        orders.setCityAging(cityAging);
//                    }
//                    //配送时效
//                    if (row.getCell(16) != null) {
//                        row.getCell(16).setCellType(CellType.NUMERIC);
//                        Double DoubleDeliveryAging = row.getCell(16).getNumericCellValue();
//                        Integer deliveryAging = DoubleDeliveryAging.intValue();
//                        orders.setDeliveryAging(deliveryAging);
//                    }
//                    //预计干线发车日期
//                    if (row.getCell(17) != null) {
//                        try {
//                            Date epectDepartDate = row.getCell(17).getDateCellValue();
//                            orders.setExpectDepartDate(epectDepartDate);
//                        } catch (Exception e) {
//                            logger.info("预计干线发车日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //预计干线到达日期
//                    if (row.getCell(18) != null) {
//                        try {
//                            Date expectArriveDate = row.getCell(18).getDateCellValue();
//                            orders.setExpectArriveDate(expectArriveDate);
//                        } catch (Exception e) {
//                            logger.info("预计干线到达日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //预计妥投日期
//                    if (row.getCell(19) != null) {
//                        try {
//                            Date expectDelieveredDate = row.getCell(19).getDateCellValue();
//                            orders.setExpectDelieveredDate(expectDelieveredDate);
//                        } catch (Exception e) {
//                            logger.info("预计妥投日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //实际干线发车日期
//                    if (row.getCell(20) != null) {
//                        try {
//                            Date actualDepartDate = row.getCell(20).getDateCellValue();
//                            orders.setActualDepartDate(actualDepartDate);
//                        } catch (Exception e) {
//                            logger.info("实际干线发车日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //实际干线到达日期
//                    if (row.getCell(21) != null) {
//                        try {
//                            Date actualArriveDate =row.getCell(21).getDateCellValue();
//                            orders.setActualArriveDate(actualArriveDate);
//                        } catch (Exception e) {
//                            logger.info("实际干线到达日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //实际妥投日期
//                    if (row.getCell(22) != null) {
//                        try {
//                            Date actualDelieveredDate =row.getCell(22).getDateCellValue();
//                            orders.setActualDelieveredDate(actualDelieveredDate);
//                        } catch (Exception e) {
//                            logger.info("实际妥投日期异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //预约送货日期
//                    if (row.getCell(23) != null) {
//                        try {
//                            Date expectDeliveryDate = row.getCell(23).getDateCellValue();
//                            orders.setExpectDeliveryDate(expectDeliveryDate);
//                        } catch (Exception e) {
//                            logger.info("预约送货时间异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //实际送货日期
//                    if (row.getCell(24) != null) {
//                        try {
//                            Date actualDeliveryDate =row.getCell(24).getDateCellValue();
//                            orders.setActualDeliveryDate(actualDeliveryDate);
//                        } catch (Exception e) {
//                            logger.info("实际送货时间异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //预约送达时间
//                    if (row.getCell(25) != null) {
//                        try {
//                            Date expectReachTime =row.getCell(25).getDateCellValue();
//                            orders.setExpectReachTime(expectReachTime);
//                        } catch (Exception e) {
//                            logger.info("预约送达时间异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //实际送达时间
//                    if (row.getCell(26) != null) {
//                        try {
//                            Date actualReachTime =row.getCell(26).getDateCellValue();
//                            orders.setActualReachTime(actualReachTime);
//                        } catch (Exception e) {
//                            logger.info("实际送达时间异常");
//                            e.printStackTrace();
//                        }
//                    }
//                    //延误原因
//                    if (row.getCell(27) != null) {
//                        row.getCell(27).setCellType(CellType.STRING);
//                        String delayReason = row.getCell(27).getStringCellValue();
//                        orders.setDelayReason(delayReason);
//                    }
//                    //自定义1   28
//
//                    //自定义2   29
//
//                    //自定义3   30
//
//                    //箱码
//                    if (row.getCell(31) != null) {
//                        row.getCell(31).setCellType(CellType.STRING);
//                        String boxNo = row.getCell(31).getStringCellValue();
//                        orderDetail.setBoxNo(boxNo);
//                    }
//                    //内件数
//                    if (row.getCell(32) != null) {
//                        row.getCell(32).setCellType(CellType.NUMERIC);
//                        Double DoubleItemsNum = row.getCell(32).getNumericCellValue();
//                        Integer itemsNum = DoubleItemsNum.intValue();
//                        orderDetail.setItemsNum(itemsNum);
//                    }
//                    //箱型
//                    if (row.getCell(33) != null) {
//                        row.getCell(33).setCellType(CellType.STRING);
//                        String boxType = row.getCell(33).getStringCellValue();
//                        orderDetail.setBoxType(boxType);
//                    }
//                    //类型 (itemType)
//                    if (row.getCell(34) != null) {
//                        row.getCell(34).setCellType(CellType.STRING);
//                        String itemType = row.getCell(34).getStringCellValue();
//                        orderDetail.setItemType(itemType);
//                    }
//                    //体积
//                    if (row.getCell(35) != null) {
//                        row.getCell(35).setCellType(CellType.NUMERIC);
//                        Double DoubleVolume = row.getCell(35).getNumericCellValue();
////                        BigDecimal volume = new BigDecimal(DoubleVolume);
//                        orderDetail.setVolume(DoubleVolume);
//                    }
//                    //自定义1 item 36
//
//                    //自定义2 item 37
//
//                    //自定义3 item 38
//
//                    //备注 item
//                    if (row.getCell(39) != null) {
//                        row.getCell(39).setCellType(CellType.STRING);
//                        String itemRemark = row.getCell(39).getStringCellValue();
//                        orderDetail.setItemRemark(itemRemark);
//                    }
//                    orderVo.setOrders(orders);
//                    orderVo.setOrderDetail(orderDetail);
//                    orderVoList.add(orderVo);
//                }
//            }
//        } catch (IOException e) {
//
//            logger.info("导入excel{}", e.getMessage());
//        }
//        return orderVoList;
//    }
//
//    public static XSSFWorkbook initExcelWorkbook(List<String> firstRow) {
//        XSSFWorkbook wb = new XSSFWorkbook();
//        XSSFSheet sheet = wb.createSheet("fieldSheet");
//        sheet.setAutobreaks(true);
//        XSSFCellStyle setBorder = wb.createCellStyle();
////        setBorder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//        setBorder.setWrapText(true);
//        XSSFRow row = sheet.createRow(0);
//
//        //设置第一行的字段
//        for (int i = 0; i < firstRow.size(); i++) {
//            row.createCell(i).setCellValue(firstRow.get(i));
//            sheet.autoSizeColumn(i);
//        }
//        return wb;
//    }
//
//    public static XSSFWorkbook exportExcel(List<LinkedHashMap> dataList, List<String> firstRow) {
//        XSSFWorkbook wb = initExcelWorkbook(firstRow);
//        XSSFSheet sheet = wb.getSheet("fieldSheet");
//        for (int i = 0; i < dataList.size(); i++) {
//            XSSFRow row = sheet.createRow(i + 1);
//            LinkedHashMap<String, Object> data = dataList.get(i);
//            for (int j = 0; j < firstRow.size(); j++) {
//                String fieldName = firstRow.get(j);
//                String value = "";
//                if (data.get(fieldName) != null) {
//                    value = String.valueOf(data.get(fieldName));
//                }
//                row.createCell(j).setCellValue(value);
//            }
//        }
//        for (int i = 0; i < firstRow.size(); i++) {
//            sheet.autoSizeColumn(i);
//        }
//        return wb;
//    }
//
//    public static void export(XSSFWorkbook wb, HttpServletResponse response, String tableName) {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        BufferedInputStream bis = null;
//        BufferedOutputStream bos = null;
//        InputStream is = null;
//        try {
//            wb.write(os);
//            byte[] content = os.toByteArray();
//            is = new ByteArrayInputStream(content);
//            response.reset();
//            response.setContentType("application/octet-stream;charset=ISO8859-1");
//            response.setHeader("Content-Disposition", "attachment;filename=" + tableName + ".xls");
//            ServletOutputStream out = response.getOutputStream();
//            bis = new BufferedInputStream(is);
//            bos = new BufferedOutputStream(out);
//            byte[] buff = new byte[2048];
//            int bytesRead;
//            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
//                bos.write(buff, 0, bytesRead);
//            }
//            response.flushBuffer();
//        } catch (IOException e) {
//            throw new RuntimeException("导出数据异常", e);
//        } finally {
//            try {
//                bos.close();
//                is.close();
//                bis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
//        Date date = new Date();
//        String dateString = simpleDateFormat.format(date);
//        System.out.println(dateString);
//
////        ExcelUtils.read("/Users/yaoyao.zhu/Documents/aaa.xls");
//    }
//
//}