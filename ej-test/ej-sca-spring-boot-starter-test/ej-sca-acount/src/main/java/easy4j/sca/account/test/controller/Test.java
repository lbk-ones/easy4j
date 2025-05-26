package easy4j.sca.account.test.controller;

import easy4j.module.base.header.EasyResult;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.json.JacksonUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class Test {


    @PostMapping("httpGet")
    @ResponseBody
    public EasyResult<String> httpGet() {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        return EasyResult.ok(JacksonUtil.toJson(ejSysProperties));
    }

    @GetMapping("index2")
    String httpGet2() {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        return "index";
    }
}
