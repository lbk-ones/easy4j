```text
easy4j.infra.common.utils.minio.EasyMinio

/**
 * 获取MinIO客户端
 */
MinioClient getMinioClient();

/**
 * 检查存储桶是否存在
 * @param bucketName 存储桶名称
 * @return 是否存在
 */
public boolean bucketExists(String bucketName);

/**
 * 为桶设置公开读策略
 * 允许匿名用户读取桶内所有对象，但禁止修改和删除
 * @param bucketName 桶名称
 */
public void setBucketPublicReadPolicy(String bucketName);

/**
 * 创建存储桶
 * @param bucketName 存储桶名称
 */
public void createBucket(String bucketName);

/**
 * 上传文件,检查存储桶是否存在，不存在则创建公开桶
 * @param bucketName 存储桶名称
 * @param file 文件
 * @param objectName 存储在MinIO中的文件名
 * @return 上传结果
 */
public String uploadFile(String bucketName, MultipartFile file, String objectName);

/**
 * 上传文件,检查存储桶是否存在，不存在则创建公开桶
 * @param bucketName 存储桶名称
 * @param file 文件
 * @param objectName 存储在MinIO中的文件名
 * @return 上传结果
 */
public String uploadFile(String bucketName, File file, String objectName);

/**
 * 上传文件（ InputStream 方式）,检查存储桶是否存在，不存在则创建
 * @param bucketName 存储桶名称
 * @param inputStream 输入流
 * @param objectName 存储在MinIO中的文件名
 * @param contentType 文件类型
 * @return 上传结果
 */
public String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType);

/**
 * 下载文件
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @param response 响应对象
 */
public void downloadFile(String bucketName, String objectName, HttpServletResponse response);

/**
 * 下载文件
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @param fileName 直接对文件命名
 * @param response 响应对象
 */
public void downloadFile(String bucketName, String objectName, String fileName, HttpServletResponse response);


/**
 * 删除文件
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @return 删除结果
 */
public boolean deleteFile(String bucketName, String objectName);

/**
 * 列出存储桶中的所有文件
 * @param bucketName 存储桶名称
 * @return 文件列表
 */
public List<String> listFiles(String bucketName);

 /**
 * 获取文件详细信息
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @return 文件信息
 */
public StatObjectResponse getFileInfo(String bucketName, String objectName);

/**
 * 检查文件是否存在
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @return 是否存在
 */
public boolean existsObject(String bucketName, String objectName);

/**
 * 获取私有桶下面的文件访问URL 最大七天
 * @param bucketName 存储桶名称
 * @param objectName 存储在MinIO中的文件名
 * @param expires 过期时间（秒）
 * @return 访问URL
 */
public String getPrivateObjectUrl(String bucketName, String objectName, int expires);
    

```
