package ej.springboot.starter.test;

import easy4j.module.base.starter.Easy4JStarterTest;
import easy4j.module.base.web.WebMvcCorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 测试webmvc
 */
//@Easy4JStarterTest(
//        serverPort = 10001,
//        serverName = "build-server",
//        serviceDesc = "测试服务",
//        author = "bokun.li",
//        enableH2 = true,
//        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
//        // 使用h2当数据库
//)
//@Import(WebMvcCorsConfig.class)
////@EnableFlowDegrade
//@SpringBootTest(classes = CorsConfigTest.class)
//@AutoConfigureMockMvc
public class CorsConfigTest {

//    @Autowired
    private MockMvc mockMvc;

//    @Test
    public void testCorsConfiguration() throws Exception {
        // 模拟来自example.com的跨域请求
        mockMvc.perform(options("/api/test")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "*"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "*"))
            .andExpect(header().string("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS"))
            .andExpect(header().string("Access-Control-Allow-Headers", "*"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
            .andExpect(header().longValue("Access-Control-Max-Age", 3600));
    }

    // 测试用的控制器
//    @RestController
    static class TestController {
//        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }
    }
}