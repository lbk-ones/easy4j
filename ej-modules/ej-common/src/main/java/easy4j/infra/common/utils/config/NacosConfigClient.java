package easy4j.infra.common.utils.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.json.JacksonUtil;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.env.PropertySource;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于Hutool HttpUtil的Nacos配置中心OpenAPI客户端
 * @author bokun.li
 * @date 2025/10/22
 */
public class NacosConfigClient {

    @Setter
    private String CONFIG_URL = "/v2/cs/config";

    // Nacos服务器基础地址（如：http://localhost:8848）
    private final String nacosServerUrl;
    // Nacos认证用户名
    private final String username;
    // Nacos认证密码
    private final String password;

    /**
     * 构造方法
     *
     * @param nacosServerUrl Nacos服务器地址（不带/nacos后缀）
     * @param username       认证用户名（null表示不开启认证）
     * @param password       认证密码（null表示不开启认证）
     */
    public NacosConfigClient(String nacosServerUrl, String username, String password) {
        if (nacosServerUrl != null && nacosServerUrl.endsWith("/")) {
            nacosServerUrl = StrUtil.replaceLast(nacosServerUrl, "/", "");
        }
        this.nacosServerUrl = nacosServerUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * 获取Nacos配置
     * 参考文档：https://nacos.io/zh-cn/docs/open-api.html
     *
     * @param dataId      配置ID（必填）
     * @param group       配置分组（默认DEFAULT_GROUP）
     * @param namespaceId 命名空间ID（默认public）
     * @param type        配置类型 默认为 properties
     * @return 配置内容
     * @throws Exception 异常信息（包含网络错误、配置不存在等）
     */
    public Map<String,Object> getConfigMap(String dataId, String group, String namespaceId, String type) {
        String config =  getConfigStr(dataId, group, namespaceId, type);
        NacosResult object = JacksonUtil.toObject(config, NacosResult.class);
        String data = object.getData();
        PropertySource<?> propertySource = StringConfigToPropertySourceUtils.autoParse("test", data);
        return StringConfigToPropertySourceUtils.toMap(propertySource);
    }

    @NotNull
    private String getConfigStr(String dataId, String group, String namespaceId, String type){
        // 1. 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("dataId", dataId);
        params.put("group", group != null ? group : "DEFAULT_GROUP");
        if (namespaceId != null && !namespaceId.isEmpty()) {
            params.put("namespaceId", namespaceId);
        }
        if(StrUtil.isNotBlank(type)){
            params.put("type", type);
        }

        // 2. 构建请求URL（Nacos配置获取接口）
        String url = nacosServerUrl + CONFIG_URL;

        // 3. 构建请求头（处理认证）
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/plain, */*");
        // 若开启认证，添加Basic Auth头
        if (username != null && password != null) {
            params.put("username",username);
            params.put("password",password);
        }

        // 4. 发送GET请求（使用Hutool的HttpRequest）
        try (HttpResponse response = HttpRequest.get(url)
                .addHeaders(headers) // 添加请求头
                .form(params) // 添加表单参数（自动编码）
                .timeout(10000) // 超时时间10秒
                .charset(StandardCharsets.UTF_8)
                .execute()) { // 执行请求

            // 5. 处理响应
            int statusCode = response.getStatus();
            String body = response.body(); // 指定编码解析响应体

            if (statusCode == 200) {
                return body;
            } else if (statusCode == 404) {
                throw new EasyException("配置不存在：dataId=" + dataId + ", group=" + group);
            } else if (statusCode == 403) {
                throw new EasyException("认证失败：用户名或密码错误");
            } else {
                throw new EasyException("获取配置失败，状态码：" + statusCode + "，响应：" + body);
            }
        }
    }

    // 示例用法
//    public static void main(String[] args) {
//        try {
//            // 初始化客户端（替换为你的Nacos信息）
//            Map<String, String> map = new NacosConfigClient(
//                    "http://localhost:8848", // Nacos地址
//                    "nacos", // 用户名
//                    "nacos"  // 密码
//            ).getConfigMap(
//                    "dataspace-elements.properties", // dataId
//                    "dataspace-service",       // group
//                    "develop",              // namespaceId（根据实际情况填写）
//                    null
//            );
//            for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
//                String key = stringStringEntry.getKey();
//                String value = stringStringEntry.getValue();
//                System.out.println(key+"="+value);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Data
    public static class NacosResult{

        private Integer code;
        private String message;
        private String data;
    }
}