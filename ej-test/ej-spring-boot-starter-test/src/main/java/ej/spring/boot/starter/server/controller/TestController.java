package ej.spring.boot.starter.server.controller;

import easy4j.module.base.header.EasyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TestController
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@RestController
public class TestController {


    @RequestMapping("hello")
    public EasyResult<String> getEasyResult() {

        log.info("this a test hello~~");
        return EasyResult.ok("hello wolrd");
    }
}