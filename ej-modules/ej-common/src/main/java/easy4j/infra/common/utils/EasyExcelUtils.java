package easy4j.infra.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * EasyExcel 工具类
 * 包含
 * 模板文件/excel文件下载
 * excel文件读取（通过类注解方式）
 * 通过模板文件填充导出
 * 通过类注解导出（可以实现模板文件下载）
 * 一个文件多sheet导出
 *
 * @author bokun.li
 * @date 2025/9/15
 */
public class EasyExcelUtils {

    private static final Logger logger = LoggerFactory.getLogger(EasyExcelUtils.class);


    /**
     * 从inputStream读取excel内容并收集成集合
     *
     * @param inputStream 输入流
     * @param sheetName 要读取的 sheet名称
     * @param recordList 要写入的记录list
     * @param headRowNumber 要跳过的表头行数
     * @param batchSize 一次读取多少条
     * @param tClass 要转换的类型
     * @param <T> 泛型约束
     */
    public static <T> void readExcelDataFromInputStream(InputStream inputStream, String sheetName, List<List<T>> recordList, int headRowNumber, final int batchSize, Class<T> tClass) {
        if (null == inputStream || StrUtil.isBlank(sheetName) || tClass == null) {
            logger.error("do nothing!!!");
            return;
        }
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        extractedExcelOperate(inputStream, sheetName, recordList, headRowNumber, batchSize, tClass);
    }

    /**
     * 从文件中读取excel内容并收集成集合
     *
     * @param filePath 文件的路径
     * @param sheetName 要读取的 sheet名称
     * @param recordList 要写入的记录list
     * @param headRowNumber 要跳过的表头行数
     * @param batchSize 一次读取多少条
     * @param tClass 要转换的类型
     * @param <T> 泛型约束
     */
    public static <T> void readExcelDataFromFilePath(String filePath, String sheetName, List<List<T>> recordList, int headRowNumber, final int batchSize, Class<T> tClass) {
        if (StrUtil.isBlank(filePath) || StrUtil.isBlank(sheetName) || tClass == null) {
            logger.error("do nothing!!!");
            return;
        }
        BufferedInputStream inputStream = FileUtil.getInputStream(filePath);
        extractedExcelOperate(inputStream, sheetName, recordList, headRowNumber, batchSize, tClass);
    }


    public static <T> void extractedExcelOperate(InputStream inputStream, String sheetName, List<List<T>> recordList, int headRowNumber, int batchSize, Class<T> tClass) {
        EasyExcel.read(inputStream, tClass, new ReadListener<T>() {
            /**
             * 单次缓存的数据量
             */
            public final int BATCH_COUNT = batchSize;
            /**
             *临时存储
             */
            private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

            @Override
            public void invoke(T data, AnalysisContext context) {
                cachedDataList.add(data);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    saveData();
                    // 存储完成清理 list
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                saveData();
            }

            @Override
            public void onException(Exception exception, AnalysisContext context) throws Exception {
                //log.error("解析失败，但是继续解析下一行:{}", exception.getMessage());
                // 如果是某一个单元格的转换异常 能获取到具体行号
                // 如果要获取头的信息 配合invokeHeadMap使用
                if (exception instanceof ExcelDataConvertException) {
                    ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
                    logger.error("第{}行，第{}列解析异常，数据为:{}", excelDataConvertException.getRowIndex(),
                            excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());
                }
            }

            /**
             * 加上存储数据库
             */
            private void saveData() {
                recordList.add(cachedDataList);
            }
        }).sheet(sheetName).headRowNumber(headRowNumber).doRead();
    }

    /**
     * 从路径下载excel（一般是下载模板）
     * @param httpServletResponse http响应
     * @param fileNameStr 下载的文件名称
     * @param templateClassPath 文件路径 如果是类路径可以写成这种 xlsxtemplate/UserImportTemplate.xlsx
     */
    public static void downloadExcelFromPath(HttpServletResponse httpServletResponse, String fileNameStr, String templateClassPath) {
        if (httpServletResponse == null || StrUtil.isBlank(fileNameStr) || StrUtil.isBlank(templateClassPath)) {
            logger.error("do nothing !!!");
            return;
        }
        try {
            httpServletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            httpServletResponse.setCharacterEncoding("UTF-8");
            // String fileName = URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\+", "%20");
            String fileName = URLEncoder.encode(fileNameStr, "UTF-8").replaceAll("\\+", "%20");
            httpServletResponse.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ClassLoader classLoader = EasyExcelUtils.class.getClassLoader();
            //InputStream resourceAsStream = classLoader.getResourceAsStream("xlsxtemplate/UserImportTemplate.xlsx");
            InputStream inputStream;
            File file = new File(templateClassPath);
            if (file.exists()) {
                inputStream = FileUtil.getInputStream(file);
            } else {
                inputStream = classLoader.getResourceAsStream(templateClassPath);
            }
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            bufferedInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            logger.error("导出错误", e);
        }
    }

    /**
     *
     * @param response http响应
     * @param templatePath 模板路径
     * @param fillObject 要填充的对象
     */
    public static void exportByTemplate(HttpServletResponse response, String templatePath, Object fillObject) {
        if (StrUtil.isBlank(templatePath) || Objects.isNull(fillObject)) {
            logger.error("nothing !!!");
            return;
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("所有用户", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ClassLoader classLoader = EasyExcelUtils.class.getClassLoader();

            // 这个流会自动关闭
            // "xlsxtemplate/UserImportTemplateFill.xlsx"
            InputStream resourceAsStream = classLoader.getResourceAsStream(templatePath);
            // 不导出 改为填充
            try (ExcelWriter build = EasyExcel.write(response.getOutputStream()).withTemplate(resourceAsStream).build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet().build();
                build.fill(fillObject, writeSheet);
            }
        } catch (IOException e) {
            if (!response.isCommitted()) {
                // 响应未提交时，尝试返回错误信息
                response.resetBuffer();
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理失败");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * 直接通过domain导出
     *
     * @param response Http响应对象
     * @param fileNameStr 文件名称
     * @param sheetName sheet名称
     * @param aclass 字节码对象
     * @param collection 要写入的集合对象
     * @param autoColumnWidth 是否自动列宽，不会太精确如果需要精确请设置为false然后在domain中手动调整
     * @param <T> 泛型
     */
    public static <T> void exportByDomain(HttpServletResponse response, String fileNameStr, String sheetName, Class<T> aclass, Collection<T> collection, boolean autoColumnWidth) {
        if (null == response || StrUtil.isBlank(fileNameStr) || StrUtil.isBlank(sheetName) || aclass == null || ListTs.isEmpty(collection)) {
            logger.error("do nothing!!");
            return;
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(fileNameStr, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ExcelWriterBuilder write = EasyExcel.write(response.getOutputStream(), aclass);
            if (autoColumnWidth) {
                write.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
            }
            write.sheet(sheetName).doWrite(collection);
        } catch (IOException e) {
            if (!response.isCommitted()) {
                // 响应未提交时，尝试返回错误信息
                response.resetBuffer();
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理失败");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }


    /**
     * 一个文件多个sheet
     *
     * @param response Http响应
     * @param fileNameStr 文件名称
     * @param sheetDatas 要输入的sheet信息 包括 sheetname class 数据等
     * @param autoColumnWidth 是否自动列宽，不会太精确如果需要精确请设置为false然后在domain中手动调整
     */
    public static void exportMultiSheetByDomain(HttpServletResponse response, String fileNameStr, List<SheetData<?>> sheetDatas, boolean autoColumnWidth) {
        if (null == response || StrUtil.isBlank(fileNameStr) || ListTs.isEmpty(sheetDatas)) {
            logger.error("do nothing!!");
            return;
        }
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(fileNameStr, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            ExcelWriterBuilder write = EasyExcel.write(response.getOutputStream());
            if (autoColumnWidth) {
                write.registerWriteHandler(new LongestMatchColumnWidthStyleStrategy());
            }
            // 这里 指定文件
            try (ExcelWriter excelWriter = write.build()) {
                for (int i = 0; i < sheetDatas.size(); i++) {
                    SheetData<?> sheetData = sheetDatas.get(i);
                    // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。这里注意DemoData.class 可以每次都变，我这里为了方便 所以用的同一个class
                    // 实际上可以一直变
                    WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetData.getSheetName()).head(sheetData.getClazz()).build();
                    // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                    excelWriter.write(sheetData.getDataList(), writeSheet);
                }
            }
        } catch (IOException e) {
            if (!response.isCommitted()) {
                // 响应未提交时，尝试返回错误信息
                response.resetBuffer();
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理失败");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @Accessors(chain = true)
    @Data
    public static class SheetData<T> {
        private String sheetName;
        private Class<T> clazz;
        private Collection<T> dataList;
    }
}
