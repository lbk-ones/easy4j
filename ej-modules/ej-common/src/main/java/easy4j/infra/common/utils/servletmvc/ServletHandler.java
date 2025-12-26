package easy4j.infra.common.utils.servletmvc;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.TypeUtils;
import easy4j.infra.common.utils.json.JacksonUtil;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Data
public class ServletHandler {

    private Method method;

    private String url;

    private Map<String, String> formDataMap;

    HttpServletRequest request;

    HttpServletResponse response;

    Object body;

    public Object getBody(Type tClass) {
        if (body != null) return body;
        StringBuilder sb = new StringBuilder();
        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)
                )
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String string = sb.toString();
            Class<?> classFromType = TypeUtils.getClassFromType(tClass);
            if (Map.class.isAssignableFrom(classFromType)) {
                Class<?>[] mapKeyAndValueType = TypeUtils.getMapKeyAndValueType(tClass);
                if (mapKeyAndValueType != null) {
                    body = JacksonUtil.toMap(string, ListTs.get(mapKeyAndValueType, 0), ListTs.get(mapKeyAndValueType, 1));
                } else {
                    body = JacksonUtil.toMap(string, String.class, Object.class);
                }
            } else if (Set.class.isAssignableFrom(classFromType)) {
                Class<?> setType = TypeUtils.getSetType(tClass);
                if (setType != null) {
                    body = JacksonUtil.toSet(string, setType);
                } else {
                    body = JacksonUtil.toSet(string, Object.class);
                }
            } else if (Collection.class.isAssignableFrom(classFromType)) {
                Class<?> collectionType = TypeUtils.getCollectionType(tClass);
                if (collectionType != null) {
                    body = JacksonUtil.toList(string, collectionType);
                } else {
                    body = JacksonUtil.toList(string, Object.class);
                }
            } else {
                body = JacksonUtil.toObject(string, classFromType);
            }
        } catch (Exception e) {
            StringBuilder sbf = new StringBuilder(e.getMessage());
            Throwable cause1 = e.getCause();
            while (cause1 != null) {
                cause1 = cause1.getCause();
                sbf.append("  ").append(e);
            }
            throw new RuntimeException(sbf.toString());
        }
        return body;
    }

    public <T> Optional<T> getFormOrQuery(Class<T> tClass) {
        if (ListTs.isNotEmpty(formDataMap)) {
            return Optional.ofNullable(JacksonUtil.toObject(JacksonUtil.toJson(formDataMap), tClass));
        }
        return Optional.empty();
    }

    /**
     * 返回json
     *
     * @param object 传参
     */
    public void responseJson(Object object) {
        if (object == null) return;
        String json;
        if (object instanceof CharSequence) {
            json = object.toString();
        } else {
            json = JacksonUtil.toJsonContainNull(object);
        }
        response.setContentType(MimeType.APPLICATION_JSON.getFullMimeTypeWithUtf8());
        PrintWriter writer;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.write(json, 0, json.length());
        writer.flush();
    }

}
