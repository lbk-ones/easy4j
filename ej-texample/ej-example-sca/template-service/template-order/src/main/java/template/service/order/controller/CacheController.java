package template.service.order.controller;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import template.service.api.client.TemplateCacheApi;

import java.util.Map;

@RequestMapping("cache")
@RestController
public class CacheController {

    @Autowired
    TemplateCacheApi templateCacheApi;


    public static final Map<String, String> CACHE = Maps.newConcurrentMap();


    @PutMapping("putCache")
    public void putCache(@RequestBody Map<String, String> cache) {
        CACHE.putAll(cache);
    }

    @GetMapping("getCache")
    public Map<String, String> getCache() {
        return CACHE;
    }

    @PutMapping("syncAll")
    public String syncAll(@RequestBody Map<String, String> cache) {
        templateCacheApi.putCache(cache);
        return "ok";
    }


}
