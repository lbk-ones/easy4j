package easy4j.infra.common.utils.servlet;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Data
public class ServletHandler {

    private Method method;

    private String url;

    private Map<String, String> formDataMap;

    HttpServletRequest request;

    HttpServletResponse response;

    public <T> T getBody(Class<T> tClass){
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
            return JacksonUtil.toObject(string,tClass);
        }catch (Exception ignored){

        }
        return null;
    }

    public <T> Optional<T> getFormOrQuery(Class<T> tClass){
        if(ListTs.isNotEmpty(formDataMap)){
            return Optional.ofNullable(JacksonUtil.toObject(JacksonUtil.toJson(formDataMap),tClass));
        }
        return Optional.empty();
    }

    /**
     * 返回json
     * @param object 传参
     */
    public void responseJson(Object object) {
        if (object == null) return;
        String json;
        if(object instanceof CharSequence){
            json = object.toString();
        }else{
            json = JacksonUtil.toJsonContainNull(object);;
        }
        response.setContentType("application/json;charset=UTF-8");
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
