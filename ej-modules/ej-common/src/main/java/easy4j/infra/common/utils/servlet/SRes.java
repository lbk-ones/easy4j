package easy4j.infra.common.utils.servlet;

import lombok.Data;

import java.io.Serializable;

@Data
public class SRes implements Serializable {

    int status;

    Object data;

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
}
