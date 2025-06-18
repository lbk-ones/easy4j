/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.sca.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.sca.Easy4jNacosInvokerApi;
import easy4j.infra.context.api.sca.NacosInvokeDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NacosServiceInvoker
 * Nacos 服务手动调用工具类 支持通过服务名发现服务并发起 HTTP 请求
 *
 * @author bokun.li
 * @date 2025/6/18
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NamingServerInvoker extends StandAbstractEasy4jResolve implements AutoRegisterContext, Easy4jNacosInvokerApi {

    // 命名空间
    private String nameSpace = "public";

    // 用户名
    private String username = "nacos";

    // 密码
    private String password = "nacos";

    // 组
    private String group = "DEFAULT_GROUP";

    // 启用https
    private boolean enableHttps = false;

    private String serverAddr = "localhost:8848";

    private RestTemplate restTemplate;

    private static final Map<String, NamingService> namingServiceCache = new ConcurrentHashMap<>();

    public NamingServerInvoker(String serverAddr) {
        this.serverAddr = serverAddr;
        this.restTemplate = new RestTemplate();
        initJackson(this.restTemplate);

    }

    public NamingServerInvoker(String nameSpace2, String serverAddr, String username, String password) {
        this.nameSpace = nameSpace2;
        this.serverAddr = serverAddr;
        this.username = username;
        this.password = password;
        this.restTemplate = new RestTemplate();
        initJackson(this.restTemplate);
    }

    public NamingServerInvoker(String serverAddr, RestTemplate restTemplate) {
        this.serverAddr = serverAddr;
        if (null == restTemplate) {
            restTemplate = new RestTemplate();
            initJackson(restTemplate);

        }
        this.restTemplate = restTemplate;
    }

    public NamingServerInvoker(String nameSpace2, String serverAddr, String username, String password, RestTemplate restTemplate) {
        this.nameSpace = nameSpace2;
        this.serverAddr = serverAddr;
        this.username = username;
        this.password = password;
        if (null == restTemplate) {
            restTemplate = new RestTemplate();
            initJackson(restTemplate);
        }
        this.restTemplate = restTemplate;
    }

    private static void initJackson(RestTemplate restTemplate) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(e -> e instanceof MappingJackson2HttpMessageConverter);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(JacksonUtil.getMapper());
        messageConverters.add(0, mappingJackson2HttpMessageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }


    /**
     * 获取 Nacos 命名服务
     */
    public NamingService getNamingService() {
        return namingServiceCache.computeIfAbsent(serverAddr, addr -> {
            try {
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                properties.put("namespace", this.nameSpace); // 可选，命名空间ID
                properties.put("username", this.username);
                properties.put("password", this.password);
                return NamingFactory.createNamingService(properties);
            } catch (NacosException e) {
                throw new RuntimeException("创建 Nacos 命名服务失败", e);
            }
        });
    }

    public String getToken(String accessToken) {

        if (StrUtil.isBlank(accessToken)) {
            Easy4jContext context = Easy4j.getContext();
            Optional<Object> threadHashValue = context.getThreadHashValue(SysConstant.X_ACCESS_TOKEN, SysConstant.X_ACCESS_TOKEN);
            return threadHashValue.map(Convert::toStr).orElse(null);
        }
        return accessToken;

    }

    /**
     * 调用远程服务（GET 请求）
     *
     * @param serviceName nacos注册服务名
     * @param group       nacos组
     * @param path        请求路径
     * @param params      请求参数
     * @param accesstoken 访问token
     * @return 响应结果
     */
    public String get(
            String serviceName,
            String group,
            String path,
            Map<String, Object> params,
            String accesstoken
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, params);
            Map<String, Object> objectObjectHashMap = Maps.newHashMap();
            objectObjectHashMap.put(SysConstant.X_ACCESS_TOKEN, getToken(accesstoken));
            MultiValueMap<String, String> multiValueMap = toMultiValueMap(objectObjectHashMap);
            HttpEntity<Object> objectHttpEntity = new HttpEntity<>(multiValueMap);
            ResponseEntity<Object> exchange = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    objectHttpEntity,
                    Object.class
            );
            return JacksonUtil.toJson(exchange.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }

    /**
     * 简单get请求
     *
     * @param serviceName
     * @param group
     * @param path
     * @return
     */
    public String get(
            String serviceName,
            String group,
            String path
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, null);
            Map<String, Object> objectObjectHashMap = Maps.newHashMap();
            objectObjectHashMap.put(SysConstant.X_ACCESS_TOKEN, getToken(null));
            MultiValueMap<String, String> multiValueMap = toMultiValueMap(objectObjectHashMap);
            HttpEntity<Object> objectHttpEntity = new HttpEntity<>(multiValueMap);
            ResponseEntity<Object> exchange = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    objectHttpEntity,
                    Object.class
            );
            return JacksonUtil.toJson(exchange.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }

    /**
     * 简单get请求
     *
     * @param serviceName
     * @param group
     * @param path
     * @return
     */
    public String putJson(
            String serviceName,
            String group,
            String path
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, null);
            Map<String, Object> objectObjectHashMap = Maps.newHashMap();
            objectObjectHashMap.put(SysConstant.X_ACCESS_TOKEN, getToken(null));
            MultiValueMap<String, String> multiValueMap = toMultiValueMap(objectObjectHashMap);
            HttpEntity<Object> objectHttpEntity = new HttpEntity<>(multiValueMap);
            ResponseEntity<Object> exchange = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    objectHttpEntity,
                    Object.class
            );
            return JacksonUtil.toJson(exchange.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }

    /**
     * 简单get请求
     *
     * @param serviceName
     * @param group
     * @param path
     * @return
     */
    public String delete(
            String serviceName,
            String group,
            String path
    ) {
        return delete(serviceName, group, path, null);
    }

    public String delete(
            String serviceName,
            String group,
            String path,
            String accessToken
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, null);
            Map<String, Object> objectObjectHashMap = Maps.newHashMap();
            objectObjectHashMap.put(SysConstant.X_ACCESS_TOKEN, getToken(accessToken));
            MultiValueMap<String, String> multiValueMap = toMultiValueMap(objectObjectHashMap);
            HttpEntity<Object> objectHttpEntity = new HttpEntity<>(multiValueMap);
            ResponseEntity<Object> exchange = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    objectHttpEntity,
                    Object.class
            );
            return JacksonUtil.toJson(exchange.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }


    /**
     * 调用远程服务（POST 请求 - 表单参数）
     *
     * @param serviceName 服务名
     * @param group       组
     * @param path        请求路径
     * @param params      请求参数
     * @return 响应结果
     */
    public String postForm(
            String serviceName,
            String group,
            String path,
            Map<String, Object> params,
            String accessToken
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, null);

            // 设置表单头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set(SysConstant.X_ACCESS_TOKEN, getToken(accessToken));

            // 转换参数
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            if (params != null) {
                params.forEach((key, value) -> formParams.add(key, value.toString()));
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);

            return JacksonUtil.toJson(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }

    /**
     * 调用远程服务（POST 请求 - JSON 参数）
     *
     * @param serviceName 服务名
     * @param group       组
     * @param path        请求路径
     * @param body        请求体
     * @param accessToken 请求token
     * @return 响应结果
     */
    public String postJson(
            String serviceName,
            String group,
            String path,
            Object body,
            String accessToken
    ) {
        try {
            Instance instance = selectInstance(serviceName, group);
            String url = buildUrl(instance, path, null);

            // 设置 JSON 头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(SysConstant.X_ACCESS_TOKEN, getToken(accessToken));

            HttpEntity<Object> request = new HttpEntity<>(body, headers);
            ResponseEntity<Object> response = restTemplate.postForEntity(url, request, Object.class);

            return JacksonUtil.toJson(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("调用服务失败", e);
        }
    }

    /**
     * 选择一个服务实例（简单随机策略）
     */
    private Instance selectInstance(String serviceName, String groupName) throws NacosException {
        if (StrUtil.isBlank(groupName)) {
            groupName = this.group;
        }
        NamingService namingService = getNamingService();
        List<Instance> instances = namingService.selectInstances(serviceName, groupName, true);

        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("找不到可用的服务实例: " + serviceName);
        }

        // 简单随机负载均衡
        Random random = new Random();
        return instances.get(random.nextInt(instances.size()));
    }

    /**
     * 构建请求 URL
     */
    private String buildUrl(Instance instance, String path, Map<String, Object> params) {
        String baseUrl = String.format(enableHttps ? "https" : "http" + "://%s:%d", instance.getIp(), instance.getPort());
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParams(toMultiValueMap(params))
                .build()
                .toUri();
        return uri.toString();
    }

    /**
     * 将 Map 转换为 MultiValueMap
     */
    private MultiValueMap<String, String> toMultiValueMap(Map<String, Object> params) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    result.add(key, value.toString());
                }
            });
        }
        return result;
    }

    public static volatile NamingServerInvoker HOLDER = null;

    public static NamingServerInvoker createByEnv(RestTemplate restTemplate) {
        if (HOLDER == null) {
            synchronized (NamingServerInvoker.class) {
                if (HOLDER == null) {
                    EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
                    String nacosNameSpace = StrUtil.blankToDefault(ejSysProperties.getNacosNameSpace(), "public");

                    String property11 = Easy4j.getProperty(SysConstant.EASY4J_SCA_NACOS_URL);
                    String username = Easy4j.getProperty(SysConstant.EASY4J_SCA_NACOS_USERNAME);
                    String password = Easy4j.getProperty(SysConstant.EASY4J_SCA_NACOS_PASSWORD);

                    String url = getUrl(property11);
                    String username1 = StrUtil.blankToDefault(getUsername(property11), username);
                    String password1 = StrUtil.blankToDefault(getPassword(property11), password);
                    HOLDER = new NamingServerInvoker(
                            nacosNameSpace,
                            url,
                            username1,
                            password1,
                            restTemplate
                    );
                }
            }
        }
        return HOLDER;
    }

    @Override
    public EasyResult<Object> get(NacosInvokeDto nacosInvokeDto) {
        String group1 = nacosInvokeDto.getGroup();
        Map<String, Object> paramMap = nacosInvokeDto.getParamMap();
        String accessToken = nacosInvokeDto.getAccessToken();
        Object body = nacosInvokeDto.getBody();
        String path = nacosInvokeDto.getPath();
        String serverName = nacosInvokeDto.getServerName();

        if (StrUtil.isNotBlank(accessToken)) {
            String s = get(serverName, group1, path, paramMap, accessToken);
            return JacksonUtil.toObject(s, new TypeReference<EasyResult<Object>>() {
            });
        } else {
            String s = get(serverName, group1, path);
            return JacksonUtil.toObject(s, new TypeReference<EasyResult<Object>>() {
            });
        }
    }

    @Override
    public EasyResult<Object> post(NacosInvokeDto nacosInvokeDto) {
        String group1 = nacosInvokeDto.getGroup();
        Map<String, Object> paramMap = nacosInvokeDto.getParamMap();
        String accessToken = nacosInvokeDto.getAccessToken();
        Object body = nacosInvokeDto.getBody();
        String path = nacosInvokeDto.getPath();
        String serverName = nacosInvokeDto.getServerName();
        if (nacosInvokeDto.isJson()) {
            String s = postJson(serverName, group1, path, body, accessToken);

            return JacksonUtil.toObject(s, new TypeReference<EasyResult<Object>>() {
            });
        } else {
            String s = postForm(serverName, group1, path, paramMap, accessToken);
            return JacksonUtil.toObject(s, new TypeReference<EasyResult<Object>>() {
            });
        }
    }

    @Override
    public EasyResult<Object> put(NacosInvokeDto nacosInvokeDto) {
        String group1 = nacosInvokeDto.getGroup();
        Map<String, Object> paramMap = nacosInvokeDto.getParamMap();
        String accessToken = nacosInvokeDto.getAccessToken();
        Object body = nacosInvokeDto.getBody();
        String path = nacosInvokeDto.getPath();
        String serverName = nacosInvokeDto.getServerName();
        if (nacosInvokeDto.isJson()) {
            String s = putJson(serverName, group1, path);
            return JacksonUtil.toObject(s, new TypeReference<EasyResult<Object>>() {
            });
        }
        return null;
    }

    @Override
    public EasyResult<Object> delete(NacosInvokeDto nacosInvokeDto) {
        String group1 = nacosInvokeDto.getGroup();
        Map<String, Object> paramMap = nacosInvokeDto.getParamMap();
        String accessToken = nacosInvokeDto.getAccessToken();
        Object body = nacosInvokeDto.getBody();
        String path = nacosInvokeDto.getPath();
        String serverName = nacosInvokeDto.getServerName();
        String delete = delete(serverName, group1, path, accessToken);
        return JacksonUtil.toObject(delete, new TypeReference<EasyResult<Object>>() {
        });
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        // 不用代理 直接这样就行
        easy4jContext.register(createByEnv(null));
    }
}