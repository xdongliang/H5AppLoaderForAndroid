package com.github.dongliang.h5apploader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dongliang on 2015/12/30.
 */
public class Utils {

    /**
     * 写入文件
     *
     * @param inputStream 下载文件的字节流对象
     * @param filePath    文件的存放目录
     */
    public static void writeFile(InputStream inputStream, String filePath) throws IOException {
        OutputStream output = null;
        try {
            //在指定目录创建一个空文件并获取文件对象
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file.createNewFile();
            //获取一个写入文件流对象
            output = new FileOutputStream(file);
            //创建一个8*1024大小的字节数组，作为循环读取字节流的临时存储空
            byte buffer[] = new byte[8*1024];
            int count;
            //循环读取下载的文件到buffer对象数组中
            while ((count = inputStream.read(buffer)) != -1) {
                //把文件写入到文件
                output.write(buffer, 0, count);
            }
        }finally {
            try {
                if (output != null) {
                    //关闭写入流
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存文本文件
     * @param filePath
     * @param content
     */
    public static void writeTextFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        FileWriter writer = null;
        try {
            if(!file.exists()){
                file.createNewFile();
            }
            writer = new FileWriter(file);
            writer.write(content);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取文本文件
     * @param filePath
     * @return
     */
    public static String readTextFile(String filePath) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        File file = new File(filePath);
        if(!file.exists())
            return "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                stringBuffer.append(tempString);
            }
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }


}
