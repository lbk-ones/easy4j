```text

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
 EasyExcelUtils.readExcelDataFromInputStream(InputStream inputStream, String sheetName, List<List<T>> recordList, int headRowNumber, final int batchSize, Class<T> tClass);
 
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
EasyExcelUtils.readExcelDataFromFilePath(String filePath, String sheetName, List<List<T>> recordList, int headRowNumber, final int batchSize, Class<T> tClass);


/**
 * 从路径下载excel（一般是下载模板）
 * @param httpServletResponse http响应
 * @param fileNameStr 下载的文件名称
 * @param templateClassPath 文件路径 如果是类路径可以写成这种 xlsxtemplate/UserImportTemplate.xlsx
 */
EasyExcelUtils.downloadExcelFromPath(HttpServletResponse httpServletResponse, String fileNameStr, String templateClassPath);

/**
 * 根据模板导出excel表格，以填充的方式
 * @param response http响应
 * @param templatePath 模板路径
 * @param fillObject 要填充的对象
 */
EasyExcelUtils.exportByTemplate(HttpServletResponse response, String templatePath, Object fillObject);


/**
 * 直接通过domain导出,单个sheet
 *
 * @param response Http响应对象
 * @param fileNameStr 文件名称
 * @param sheetName sheet名称
 * @param aclass 字节码对象
 * @param collection 要写入的集合对象
 * @param autoColumnWidth 是否自动列宽，不会太精确如果需要精确请设置为false然后在domain中手动调整
 * @param <T> 泛型
 */
EasyExcelUtils.exportByDomain(HttpServletResponse response, String fileNameStr, String sheetName, Class<T> aclass, Collection<T> collection, boolean autoColumnWidth);


/**
 * 直接通过domain导出，一个文件多个sheet
 *
 * @param response Http响应
 * @param fileNameStr 文件名称
 * @param sheetDatas 要输入的sheet信息 包括 sheetname class 数据等
 * @param autoColumnWidth 是否自动列宽，不会太精确如果需要精确请设置为false然后在domain中手动调整
 */
EasyExcelUtils.exportMultiSheetByDomain(HttpServletResponse response, String fileNameStr, List<SheetData<?>> sheetDatas, boolean autoColumnWidth);

```