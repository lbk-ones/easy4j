package easy4j.infra.common.utils.minio;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 * 单独运行这个类 程序可能会挂起 久久无法关闭 正常现象无需惊讶
 * @author bokun.li
 */
@Slf4j
public class EasyMinio {

    // MinIO服务器地址
    private final String url;

    // 访问密钥
    private final String accessKey;

    // 密钥
    private final String secretKey;

    public EasyMinio(String url, String accessKey, String secretKey) {
        if (StrUtil.isNotBlank(url) && !(url.startsWith("http://") || url.startsWith("https://"))) {
            url = "http://" + url;
        }
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    // 单例实例
    private static volatile MinioClient minioClient;

    /**
     * 初始化MinIO客户端
     */
    public MinioClient getMinioClient() {
        if (minioClient == null) {
            synchronized (EasyMinio.class) {
                if (minioClient == null) {
                    minioClient = MinioClient.builder()
                            .endpoint(url)
                            .credentials(accessKey, secretKey)
                            .build();
                }
            }
        }
        return minioClient;
    }

    public String getBaseUrl() {
        return url;
    }

    /**
     * 检查存储桶是否存在
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    public boolean bucketExists(String bucketName) throws Exception {
        return getMinioClient().bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }


    /**
     * 为桶设置公开读策略
     * 允许匿名用户读取桶内所有对象，但禁止修改和删除
     * @param bucketName 桶名称
     */
    public void setBucketPublicReadPolicy(String bucketName) throws Exception {
        if (!bucketExists(bucketName)) {
            return;
        }
        // 定义公开读的访问策略JSON
        String publicReadPolicy = "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": \"*\",\n" +  // 所有用户（匿名用户）
                "      \"Action\": \"s3:GetObject\",\n" +  // 允许读取对象
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +  // 桶内所有对象
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            // 应用策略到桶
            getMinioClient().setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(publicReadPolicy)
                            .build()
            );
        } catch (Exception e) {
            log.error("public policy error", e);
        }

    }


    /**
     * 创建存储桶
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) throws Exception {
        if (!bucketExists(bucketName)) {
            getMinioClient().makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            setBucketPublicReadPolicy(bucketName);
        }
    }

    /**
     * 上传文件,检查存储桶是否存在，不存在则创建公开桶
     * @param bucketName 存储桶名称
     * @param file 文件
     * @param objectName 存储在MinIO中的文件名
     * @return 上传结果
     */
    public String uploadFile(String bucketName, MultipartFile file, String objectName) throws Exception {
        // 检查存储桶是否存在，不存在则创建
        createBucket(bucketName);

        try (InputStream inputStream = file.getInputStream()) {
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return SP.SLASH+bucketName+SP.SLASH+objectName;
        }
    }

    /**
     * 上传文件,检查存储桶是否存在，不存在则创建公开桶
     * @param bucketName 存储桶名称
     * @param file 文件
     * @param objectName 存储在MinIO中的文件名
     * @return 上传结果
     */
    public String uploadFile(String bucketName, File file, String objectName) throws Exception {
        // 检查存储桶是否存在，不存在则创建
        createBucket(bucketName);
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.length(), -1)
                            .contentType(Files.probeContentType(Paths.get(file.getAbsolutePath())))
                            .build()
            );
            return SP.SLASH+bucketName+SP.SLASH+objectName;
        }
    }

    /**
     * 上传文件（ InputStream 方式）,检查存储桶是否存在，不存在则创建
     * @param bucketName 存储桶名称
     * @param inputStream 输入流
     * @param objectName 存储在MinIO中的文件名
     * @param contentType 文件类型
     * @return 上传结果
     */
    public String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType) throws Exception {
        createBucket(bucketName);

        getMinioClient().putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, -1, 10485760) // 10MB 分片
                        .contentType(contentType)
                        .build()
        );
        return SP.SLASH+bucketName+SP.SLASH+objectName;
    }

    /**
     * 下载文件
     * @param bucketName 存储桶名称
     * @param objectName 存储在MinIO中的文件名
     * @param response 响应对象
     */
    public void downloadFile(String bucketName, String objectName, HttpServletResponse response) throws Exception {
        // 检查文件是否存在
        if (!existsObject(bucketName, objectName)) {
            throw new Exception("文件不存在");
        }

        try (InputStream stream = getMinioClient().getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {

            // 设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(objectName, "UTF-8"));
            response.setContentType("application/octet-stream");

            IoUtil.copy(stream,response.getOutputStream(),8092);

        } catch (IOException e) {
            throw new Exception("文件下载失败");
        }
    }

    /**
     * 删除文件
     * @param bucketName 存储桶名称
     * @param objectName 存储在MinIO中的文件名
     * @return 删除结果
     */
    public boolean deleteFile(String bucketName, String objectName) throws Exception {
        if (!existsObject(bucketName, objectName)) {
            return false;
        }

        getMinioClient().removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        return true;
    }

    /**
     * 列出存储桶中的所有文件
     * @param bucketName 存储桶名称
     * @return 文件列表
     */
    public List<String> listFiles(String bucketName) throws Exception {
        List<String> fileList = new ArrayList<>();

        Iterable<Result<Item>> results = getMinioClient().listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> result : results) {
            Item item = result.get();
            if (!item.isDir()) {
                fileList.add(item.objectName());
            }
        }

        return fileList;
    }

    /**
     * 获取文件详细信息
     * @param bucketName 存储桶名称
     * @param objectName 存储在MinIO中的文件名
     * @return 文件信息
     */
    public StatObjectResponse getFileInfo(String bucketName, String objectName) throws Exception {
        if (!existsObject(bucketName, objectName)) {
            return null;
        }

        return getMinioClient().statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 检查文件是否存在
     * @param bucketName 存储桶名称
     * @param objectName 存储在MinIO中的文件名
     * @return 是否存在
     */
    public boolean existsObject(String bucketName, String objectName) throws Exception {
        if (!bucketExists(bucketName)) {
            return false;
        }

        try {
            getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取私有桶下面的文件访问URL 最大七天
     * @param bucketName 存储桶名称
     * @param objectName 存储在MinIO中的文件名
     * @param expires 过期时间（秒）
     * @return 访问URL
     */
    public String getPrivateObjectUrl(String bucketName, String objectName, int expires) throws Exception {
        if (!existsObject(bucketName, objectName)) {
            return null;
        }
        return getMinioClient().getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(expires, TimeUnit.SECONDS)
                        .build()
        );
    }
}
    