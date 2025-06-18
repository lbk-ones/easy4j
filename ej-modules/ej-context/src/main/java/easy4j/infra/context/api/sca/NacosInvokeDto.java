package easy4j.infra.context.api.sca;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NacosInvokeDto {

    private String serverName;

    private String group;

    private String path;

    private Object body;

    private String accessToken;

    private Map<String, Object> paramMap;

    private boolean isJson;


}
