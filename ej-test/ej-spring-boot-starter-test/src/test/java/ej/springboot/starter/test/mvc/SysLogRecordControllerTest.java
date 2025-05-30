package ej.springboot.starter.test.mvc;

import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.starter.Easy4JStarterTest;
import ej.spring.boot.starter.server.StartTest;
import ej.spring.boot.starter.server.service.SysLogRecordService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Easy4JStarterTest(
        serverPort = 10001,
        serverName = "build-server",
        serviceDesc = "测试服务",
        author = "bokun.li",
        enableH2 = true
        //ejDataSourceUrl = "jdbc:mysql://localhost:3306/vcc_portal_v1@root:123456",
        // 使用h2当数据库
)
@SpringBootTest(classes = StartTest.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class SysLogRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysLogRecordService sysLogRecordService;


    @Test
    void testCreateLogRecord() throws Exception {
        // 准备测试数据
        SysLogRecord logRecord = new SysLogRecord();
        logRecord.setId("1");
        logRecord.setTag("TEST");
        logRecord.setCreateDate(new Date());


        // 模拟服务层行为
        when(sysLogRecordService.save(logRecord)).thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/api/sys/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"TEST\"}"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.log())
                .andExpect(jsonPath("$.data.id").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.tag").value("TEST"));

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).save(any(SysLogRecord.class));
    }

    @Test
    void testGetLogRecordById() throws Exception {
        // 准备测试数据
        SysLogRecord logRecord = new SysLogRecord();
        logRecord.setId("1");
        logRecord.setTag("TEST");

        // 模拟服务层行为
        when(sysLogRecordService.save(logRecord)).thenReturn(true);
        when(sysLogRecordService.getById("1")).thenReturn(logRecord);

        // 执行请求
        mockMvc.perform(get("/api/sys/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.tag").value("TEST"));

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).getById("1");
    }

    @Test
    void testGetLogRecordByIdNotFound() throws Exception {
        // 模拟服务层行为
        when(sysLogRecordService.getById("999")).thenReturn(null);

        // 执行请求
        mockMvc.perform(get("/api/sys/logs/999"))
                .andExpect(status().isOk());

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).getById("999");
    }

    @Test
    void testGetAllLogRecords() throws Exception {
        // 准备测试数据
        SysLogRecord log1 = new SysLogRecord();
        log1.setId("1");
        log1.setTag("TEST1");

        SysLogRecord log2 = new SysLogRecord();
        log2.setId("2");
        log2.setTag("TEST2");

        // 模拟服务层行为
        when(sysLogRecordService.list()).thenReturn(Arrays.asList(log1, log2));

        // 执行请求
        mockMvc.perform(get("/api/sys/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[1].id").value("2"));

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).list();
    }

    @Test
    void testUpdateLogRecord() throws Exception {
        // 准备测试数据
        SysLogRecord existingRecord = new SysLogRecord();
        existingRecord.setId("1");
        existingRecord.setTag("OLD");
        existingRecord.setRemark("旧备注");

        SysLogRecord updatedRecord = new SysLogRecord();
        updatedRecord.setId("1");
        updatedRecord.setTag("NEW");
        updatedRecord.setRemark("新备注");

        // 模拟服务层行为
        when(sysLogRecordService.getById("1")).thenReturn(null);
        when(sysLogRecordService.save(existingRecord)).thenReturn(true);

        // 执行请求
        mockMvc.perform(put("/api/sys/logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"NEW\",\"remark\":\"新备注\"}"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.log())
                .andExpect(jsonPath("$.data.tag").value("NEW"))
                .andExpect(jsonPath("$.data.remark").value("新备注"));

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).getById("1");
        verify(sysLogRecordService, times(1)).save(any(SysLogRecord.class));
    }

    @Test
    void testUpdateLogRecordNotFound() throws Exception {
        // 模拟服务层行为
        when(sysLogRecordService.getById("999")).thenReturn(null);

        // 执行请求
        mockMvc.perform(put("/api/sys/logs/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tag\":\"NEW\"}"))
                .andExpect(status().isNotFound());

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).getById("999");
        verify(sysLogRecordService, never()).save(any(SysLogRecord.class));
    }

//    @Test
//    void testDeleteLogRecord() throws Exception {
//        // 模拟服务层行为
//        when(sysLogRecordService.existsById("1")).thenReturn(true);
//        doNothing().when(sysLogRecordService).deleteById("1");
//
//        // 执行请求
//        mockMvc.perform(delete("/api/sys/logs/1"))
//                .andExpect(status().isNoContent());
//
//        // 验证服务层方法被调用
//        verify(sysLogRecordService, times(1)).existsById("1");
//        verify(sysLogRecordService, times(1)).deleteById("1");
//    }
//
//    @Test
//    void testDeleteLogRecordNotFound() throws Exception {
//        // 模拟服务层行为
//        when(sysLogRecordService.existsById("999")).thenReturn(false);
//
//        // 执行请求
//        mockMvc.perform(delete("/api/sys/logs/999"))
//                .andExpect(status().isNotFound());
//
//        // 验证服务层方法被调用
//        verify(sysLogRecordService, times(1)).existsById("999");
//        verify(sysLogRecordService, never()).deleteById(anyString());
//    }
//
//    @Test
//    void testGetLogRecordsByTag() throws Exception {
//        // 准备测试数据
//        SysLogRecord log1 = new SysLogRecord();
//        log1.setId("1");
//        log1.setTag("TEST");
//
//        SysLogRecord log2 = new SysLogRecord();
//        log2.setId("2");
//        log2.setTag("TEST");
//
//        // 模拟服务层行为
//        when(sysLogRecordService.findByTag("TEST")).thenReturn(Arrays.asList(log1, log2));
//
//        // 执行请求
//        mockMvc.perform(get("/api/sys/logs/tag/TEST"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].tag").value("TEST"))
//                .andExpect(jsonPath("$[1].tag").value("TEST"));
//
//        // 验证服务层方法被调用
//        verify(sysLogRecordService, times(1)).findByTag("TEST");
//    }
//
//    @Test
//    void testGetLogRecordsByOperateCode() throws Exception {
//        // 准备测试数据
//        SysLogRecord log1 = new SysLogRecord();
//        log1.setId("1");
//        log1.setOperateCode("USER001");
//
//        SysLogRecord log2 = new SysLogRecord();
//        log2.setId("2");
//        log2.setOperateCode("USER001");
//
//        // 模拟服务层行为
//        when(sysLogRecordService.findByOperateCode("USER001")).thenReturn(Arrays.asList(log1, log2));
//
//        // 执行请求
//        mockMvc.perform(get("/api/sys/logs/operator/USER001"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$[0].operateCode").value("USER001"))
//                .andExpect(jsonPath("$[1].operateCode").value("USER001"));
//
//        // 验证服务层方法被调用
//        verify(sysLogRecordService, times(1)).findByOperateCode("USER001");
//    }


//    @Configuration
//    public static class WebMvcFilterTestConfig implements WebMvcConfigurer {
//
//        @Autowired
//        PerRequestInterceptor perRequestInterceptor;
//
//        @Override
//        public void addInterceptors(InterceptorRegistry registry) {
//            registry.addInterceptor(perRequestInterceptor)
//                    .order(Integer.MIN_VALUE)
//                    .pathMatcher(new AntPathMatcher("/**"));
//        }
//    }

}    