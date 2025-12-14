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
package easy4j.module.sca.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.common.utils.json.JacksonUtil;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * http 工具类 获取请求中的参数
 */
@Slf4j
public class HttpUtils {

    /**
     * 将URL的参数和body参数合并
     */
    public static SortedMap<String, String> getAllParams(HttpServletRequest request) throws IOException {

        SortedMap<String, String> result = new TreeMap<>();
        // 获取URL上最后带逗号的参数变量 sys/dict/getDictItems/sys_user,realname,username
        String pathVariable = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);
        if (pathVariable.contains(StringPool.COMMA)) {
            log.info("pathVariable: {}", pathVariable);
            String deString = URLDecoder.decode(pathVariable, "UTF-8");
            log.info("pathVariable decode: {}", deString);
            result.put(SignUtil.X_PATH_VARIABLE, deString);
        }
        // 获取URL上的参数
        Map<String, String> urlParams = getUrlParams(request);
        result.putAll(urlParams);
        Map<String, String> allRequestParam = new HashMap<>(16);
        // get请求不需要拿body参数
        if (!HttpMethod.GET.name().equals(request.getMethod())) {
            allRequestParam = getAllRequestParam(request);
        }
        // 将URL的参数和body参数进行合并
        if (allRequestParam != null) {
            result.putAll(allRequestParam);
        }
        return result;
    }

    /**
     * 将URL的参数和body参数合并
     */
    public static SortedMap<String, String> getAllParams(String url, String queryString, byte[] body, String method)
            throws IOException {

        SortedMap<String, String> result = new TreeMap<>();
        // 获取URL上最后带逗号的参数变量 sys/dict/getDictItems/sys_user,realname,username
        String pathVariable = url.substring(url.lastIndexOf("/") + 1);
        if (pathVariable.contains(StringPool.COMMA)) {
            log.info(" pathVariable: {}", pathVariable);
            String deString = URLDecoder.decode(pathVariable, "UTF-8");
            log.info(" pathVariable decode: {}", deString);
            result.put(SignUtil.X_PATH_VARIABLE, deString);
        }
        // 获取URL上的参数
        Map<String, String> urlParams = getUrlParams(queryString);
        result.putAll(urlParams);
        Map<String, String> allRequestParam = new HashMap<>(16);
        // get请求不需要拿body参数
        if (!HttpMethod.GET.name().equals(method)) {
            allRequestParam = getAllRequestParam(body);
        }
        // 将URL的参数和body参数进行合并
        if (allRequestParam != null) {
            result.putAll(allRequestParam);
        }
        return result;
    }

    /**
     * 获取 Body 参数
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String str = "";
        StringBuilder wholeStr = new StringBuilder();
        // 一行一行的读取body体里面的内容；
        while ((str = reader.readLine()) != null) {
            wholeStr.append(str);
        }
        // 转化成json对象
        return JacksonUtil.toMap(wholeStr.toString(), String.class, String.class);
    }

    /**
     * 获取 Body 参数
     */
    public static Map<String, String> getAllRequestParam(final byte[] body) throws IOException {
        if (body == null) {
            return null;
        }
        String wholeStr = new String(body);
        // 转化成json对象
        return JacksonUtil.toObject(wholeStr, new TypeReference<Map<String, String>>() {
        });
    }

    /**
     * 将URL请求参数转换成Map
     *
     * @param request
     */
    public static Map<String, String> getUrlParams(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>(16);
        if (StringUtils.isEmpty(request.getQueryString())) {
            return result;
        }
        String param = "";
        try {
            param = URLDecoder.decode(request.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] params = param.split("&");
        for (String s : params) {
            int index = s.indexOf("=");
            if (index != -1) {
                result.put(s.substring(0, index), s.substring(index + 1));
            }
        }
        return result;
    }

    /**
     * 将URL请求参数转换成Map
     *
     * @param queryString
     */
    public static Map<String, String> getUrlParams(String queryString) {
        Map<String, String> result = new HashMap<>(16);
        if (StringUtils.isEmpty(queryString)) {
            return result;
        }
        String param = "";
        try {
            param = URLDecoder.decode(queryString, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] params = param.split("&");
        for (String s : params) {
            int index = s.indexOf("=");
            if (index != -1) {
                result.put(s.substring(0, index), s.substring(index + 1));
            }
        }
        return result;
    }
}
