package com.bsd.say.util;///**
// * Copyright (C), 1999-2018, 锦铭泰软件有限公司
// * FileName: ExcelUtil
// * Author:   周金明
// * Date:     2018/10/9 12:04
// * Description: ExcelUtil
// * History:
// * <author>          <time>          <version>          <desc>
// * 周金明           修改时间           版本号              描述
// */
//package com.oceanspot.template.util;
//
//import java.io.BufferedOutputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.util.StringUtil;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.web.multipart.MultipartFile;
//
///**
// * 〈一句话功能简述〉
// * 〈ExcelUtil〉
// *
// * @author 周金明
// * @create 2018/10/9
// * @since 1.0.0
// */
//public class ExcelUtil {
//    @SuppressWarnings("resource")
//    public static void readExcel(int row, String fileName, InputStream is, List<Map<Integer, String>> data_list, Map<Integer, String> head) {
//        // 是否是excel2007
//        boolean is2007 = fileName.endsWith("xlsx");
//        try {
//            // 建立输入流
//            Workbook wb = null;
//            // 根据文件格式(2003或者2007)来初始化
//            if (is2007)
//                wb = new XSSFWorkbook(is);
//            else
//                wb = new HSSFWorkbook(is);
//
//            // 获得第一个表单
//            Sheet sheet = wb.getSheetAt(0);
//
//            // 读取抬头
//            getHead(head, sheet.getRow(row));
//
//            // 读取数据
//            getData(row + 1, data_list, head, sheet);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    // 取抬头信息
//    public static void getHead(Map<Integer, String> head, Row first_row) {
//
//        // 循环取第一行数据
//        int i = 0;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String col_name = getCellString(first_row.getCell(i), sdf);
//        while (StringUtils.isNotEmpty(col_name)) {
//            head.put(i, col_name);
//            i++;
//            col_name = getCellString(first_row.getCell(i), sdf);
//        }
//
//        // 判断表头是否为空
//        if (head == null || head.size() == 0) {
//            throw new RuntimeException("Excel表中第一行为抬头数据，不允许为空！");
//        }
//    }
//
//    // 取数据
//    public static void getData(int i, List<Map<Integer, String>> data_list, Map<Integer, String> head, Sheet sheet) {
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        // 从第二行开始读取数据
//        int col_cnt = head.size();
//        while (true) {
//            Row row = sheet.getRow(i);
//            // 如果第一列为空，就认为已经读取完成
//            if (row == null || row.getCell(0) == null || StringUtil.isBlankOrNull(getCellString(row.getCell(0), sdf))) {
//                return;
//            }
//
//            // 读取数据
//            Map<Integer, String> data = new HashMap<>();
//            for (int j = 0; j < col_cnt; j++) {
//                Cell cell = row.getCell(j);
//                if (cell == null) {
//                    data.put(j, "");
//                } else {
//                    data.put(j, getCellString(cell, sdf));
//                }
//            }
//            data_list.add(data);
//            i++;
//        }
//    }
//
//    // 取单元格数据
//    @SuppressWarnings("deprecation")
//    public static String getCellString(Cell cell, SimpleDateFormat sdf) {
//
//        if (cell == null) {
//            return "";
//        }
//
//        switch (cell.getCellType()) {
//            case Cell.CELL_TYPE_STRING:// 字符
//                return cell.getStringCellValue().toUpperCase();
//            case Cell.CELL_TYPE_BLANK:// 空
//                return cell.getStringCellValue();
//            case Cell.CELL_TYPE_BOOLEAN:// 布尔
//                return cell.getBooleanCellValue() ? "1" : "0";
//            case Cell.CELL_TYPE_NUMERIC:// 数值，时间
//                if (HSSFDateUtil.isCellDateFormatted(cell)) {
//                    return sdf.format(cell.getDateCellValue());
//                } else {
//                    String cell_str = String.valueOf(cell.getNumericCellValue());
//                    if (cell_str.endsWith(".0")) {
//                        cell_str = cell_str.substring(0, cell_str.length() - 2);
//                    }
//                    return cell_str;
//                }
//            default:
//                throw new RuntimeException("表格第" + cell.getRowIndex() + "行，第" + cell.getColumnIndex() + "列的格式不正确！");
//        }
//    }
//
//    // 读取文件信息
//    public static File getFile(String excel, String userId) throws IOException {
//
//        String path = getRealPath();// 获取类路径
//        path = path.replace("\\", "/");// 路径转意
//
//        String user_id = userId + getPid();// 得到随机数
//
//        // 给文件命名，保证不重复
//        if (StringUtil.compare(excel.substring(0, 6), "0M8R4K")) {
//            path = path + "/" + user_id + ".xls";
//        } else {
//            path = path + "/" + user_id + ".xlsx";
//        }
//
//        // 把数据转换成字节文件
//        byte[] buffer = File2byte(excel);//new BASE64Decoder().decodeBuffer(excel);
//
//        // 指定写入路径
//        FileOutputStream out = new FileOutputStream(path);
//
//        // 开始写入文件到指定路径
//        out.write(buffer);
//        out.close();
//
//        // 创建file对象
//        File file = new File(path);
//
//        return file;
//    }
//
//    // 获取类路径
//    public static String getRealPath() {
//        String realPath = ExcelUtil.class.getClassLoader().getResource("").getFile();
//        File file = new File(realPath);
//        realPath = file.getAbsolutePath();
//        try {
//            realPath = java.net.URLDecoder.decode(realPath, "utf-8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return realPath;
//    }
//
//    // 生成指定id 日期+6位随机数
//    public static String getPid() {
//        String param_id = "";
//        // 指定日期格式
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//        param_id += sdf.format(new Date());
//        // System.out.println(param_id);
//        // 生成随机数
//        Random random = new Random();
//        int num = random.nextInt(999999);
//        String str = String.format("%6d", num).replace(" ", "0");
//        param_id = param_id + str;
//        return param_id;
//    }
//
//    /**
//     * @param bytes
//     * @return
//     */
//    public static byte[] decode(final byte[] bytes) {
//        return Base64.decodeBase64(bytes);
//    }
//
//    /**
//     * 二进制数据编码为BASE64字符串
//     *
//     * @param bytes
//     * @return
//     * @throws Exception
//     */
//    public static String encode(final byte[] bytes) {
//        return new String(Base64.encodeBase64(bytes));
//    }
//
//    public static byte[] File2byte(String filePath) {
//        byte[] buffer = null;
//        try {
//            File file = new File(filePath);
//            FileInputStream fis = new FileInputStream(file);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            byte[] b = new byte[1024];
//            int n;
//            while ((n = fis.read(b)) != -1) {
//                bos.write(b, 0, n);
//            }
//            fis.close();
//            bos.close();
//            buffer = bos.toByteArray();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return buffer;
//    }
//
//    public static List<Map<Integer, String>> storeExcelFile(MultipartFile excelFile) throws IOException {
//        String tempPath = System.getProperty("java.io.tmpdir") + "\\";
//        String fileName = excelFile.getOriginalFilename();
//        File tmpFile = new File(tempPath + fileName);
//        excelFile.transferTo(tmpFile);
//        int readRow = 0;
//        // excel数据
//        List<Map<Integer, String>> dataList = new ArrayList<Map<Integer, String>>();
//        // excel表头
//        Map<Integer, String> head = new HashMap<>();
//        ExcelUtil.readExcel(readRow, fileName, new FileInputStream(tmpFile), dataList, head);
//        return dataList;
//    }
//
//    private byte[] InputStream2ByteArray(String filePath) throws IOException {
//
//        InputStream in = new FileInputStream(filePath);
//        byte[] data = toByteArray(in);
//        in.close();
//
//        return data;
//    }
//
//    private byte[] toByteArray(InputStream in) throws IOException {
//
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024 * 4];
//        int n = 0;
//        while ((n = in.read(buffer)) != -1) {
//            out.write(buffer, 0, n);
//        }
//        return out.toByteArray();
//    }
//
//    public static void byte2File(byte[] buf, String filePath, String fileName) {
//        BufferedOutputStream bos = null;
//        FileOutputStream fos = null;
//        File file = null;
//        try {
//            File dir = new File(filePath);
//            if (!dir.exists() && dir.isDirectory()) {
//                dir.mkdirs();
//            }
//            file = new File(filePath + File.separator + fileName);
//            fos = new FileOutputStream(file);
//            bos = new BufferedOutputStream(fos);
//            bos.write(buf);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (bos != null) {
//                try {
//                    bos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}
