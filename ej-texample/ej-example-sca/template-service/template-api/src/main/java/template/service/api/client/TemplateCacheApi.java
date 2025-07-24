package template.service.api.client;


import easy4j.infra.common.header.EasyResult;
import easy4j.module.sca.broadcast.BroadcastFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "template-order", configuration = BroadcastFeign.class)
public interface TemplateCacheApi {

    @PutMapping("/cache/putCache")
    EasyResult<Object> putCache(@RequestBody Map<String, String> cache);
}
