package easy4j.infra.common.utils.servletmvc;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.json.JacksonUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class SRes implements Serializable {

    int status;

    Object data;

    String message;

    public static SRes success(Object data){
        SRes sRes = new SRes();
        sRes.setData(data);
        sRes.setStatus(1);
        return sRes;
    }

    public static SRes error(Object data){
        SRes sRes = new SRes();
        sRes.setData(data);
        sRes.setStatus(0);
        return sRes;
    }
    public static SRes error(String data){
        SRes sRes = new SRes();
        sRes.setData(null);
        sRes.setMessage(StrUtil.blankToDefault(data,"系统错误"));
        sRes.setStatus(0);
        return sRes;
    }

    @Override
    public String toString() {
        return JacksonUtil.toJsonContainNull(this);
    }
}
