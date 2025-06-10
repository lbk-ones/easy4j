package ej.spring.boot.nacos;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        // 确保目标目录存在
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        // 创建解压输入流
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();

            // 遍历ZIP文件中的每个条目
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();

                // 如果是目录，创建目录
                if (!entry.isDirectory()) {
                    extractFile(zipIn, filePath);
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }

                // 关闭当前条目，准备处理下一个
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        // 确保父目录存在
        File parentDir = new File(filePath).getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 写入文件内容
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    // 使用示例
    public static void main(String[] args) {
        try {
            unzip("path/to/your/file.zip", "path/to/extract");
            System.out.println("解压完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}