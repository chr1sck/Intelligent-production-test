package com.bsd.say.util;


import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.Map;

/**
 * 文件操作工具类
 */
public class FileUtils {


    public static void read(String path, Map map) {

        BufferedReader br = null;
        InputStreamReader reader = null;
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

                /* 读入TXT文件 */
//            String pathname = "C:\\360Downloads\\input.txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(path); // 要读取以上路径的input。txt文件
            reader = new InputStreamReader(
                    new FileInputStream(filename), "utf-8"); // 建立一个输入流对象reader
            br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            line = br.readLine();
            int count = 0;
            while (line != null) {
                line = br.readLine(); // 一次读入一行数据
                count++;
                map.put(count, line);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (br != null) try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static void uploadImage(byte[] file, String filePath, String fileName) throws Exception {

        File targetFile = new File(filePath);

        System.out.println("-----"+filePath+fileName);

        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }


        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
//        WaterMarkUtils.mark(filePath+fileName, filePath+fileName, Color.YELLOW, DateUtils.getCurDateTimeString());
        out.flush();
        out.close();
    }

    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {

        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }

    /**
     *
     * @param absPath
     * @param newFilePath
     */
    public static void copyFile(String absPath, String newFilePath) throws IOException {

        File orignal = new File(absPath);
        File newFile = new File(newFilePath);
        FileCopyUtils.copy(orignal, newFile);
    }
}
