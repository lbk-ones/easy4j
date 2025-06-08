/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ej.springboot.starter.test.mvc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import ej.spring.boot.starter.server.controller.SysLogRecordController;
import ej.spring.boot.starter.server.service.SysLogRecordService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysLogRecordControllerTest2
 *
 * @author bokun.li
 * @date 2025-05-31 00:54:11
 */
@WebMvcTest(controllers = SysLogRecordController.class)
@ContextConfiguration(classes = SysLogRecordControllerTest.class)
@Import({SysLogRecordControllerTest.Config.class})
public class SysLogRecordControllerTest {


    @Configuration
    public static class Config {
        @Bean
        public SysLogRecordController sysLogRecordController() {
            return new SysLogRecordController();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SysLogRecordService sysLogRecordService;


    // ============================== 创建日志记录 ==============================
    @Test
    public void testCreateLogRecord_Success() throws Exception {
        // 准备测试数据
        SysLogRecord requestLog = new SysLogRecord();
        requestLog.setTag("API");
        requestLog.setOperateCode("USER_001");

        SysLogRecord savedLog = new SysLogRecord();
        savedLog.setId("1");
        savedLog.setTag("API");
        savedLog.setCreateDate(new Date());
        savedLog.setOperateCode("USER_001");

        // 模拟服务层返回成功
        when(sysLogRecordService.save(any(SysLogRecord.class))).thenReturn(true);

        // 发送 POST 请求
        mockMvc.perform(post("/api/sys/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestLog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data.tag").value("API"))
                .andExpect(jsonPath("$.data.operateCode").value("USER_001"));

        // 验证服务层方法被调用
        verify(sysLogRecordService, times(1)).save(any(SysLogRecord.class));
    }

    // ============================== 分页查询日志 ==============================
    @Test
    public void testListLogRecords_Pagination() throws Exception {
        // 准备分页数据
        Page<SysLogRecord> pageRequest = new Page<>(1, 10);
        IPage<SysLogRecord> pageResult = new Page<>();
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId("1");
        sysLogRecord.setTag("TAG1");
        SysLogRecord sysLogRecord2 = new SysLogRecord();
        sysLogRecord2.setId("1");
        sysLogRecord2.setTag("TAG1");
        pageResult.setRecords(Arrays.asList(
                sysLogRecord,
                sysLogRecord2
        ));
        pageResult.setTotal(100);

        LambdaQueryWrapper<SysLogRecord> sysLogRecordLambdaQueryWrapper = new LambdaQueryWrapper<SysLogRecord>().orderByDesc(SysLogRecord::getCreateDate);
        // 模拟服务层返回分页结果
        when(sysLogRecordService.page(any(), any())).thenReturn(pageResult);

        // 发送 GET 请求
        mockMvc.perform(get("/api/sys/logs")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value("1"));

        // 验证排序条件（按创建时间倒序）
        verify(sysLogRecordService, times(1)).page(
                any(),
                any()
        );
    }

    // ============================== 根据 ID 查询 ==============================
    @Test
    public void testGetLogRecordById_ExistentId() throws Exception {
        // 准备存在的日志记录
        SysLogRecord log = new SysLogRecord();
        log.setId("1");
        log.setTag("TAG");
        log.setCreateDate(new Date());
        when(sysLogRecordService.getById("1")).thenReturn(log);

        // 发送 GET 请求
        mockMvc.perform(get("/api/sys/logs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("1"));
    }

    @Test
    public void testGetLogRecordById_NonExistentId() throws Exception {
        // 模拟服务层返回 null
        when(sysLogRecordService.getById("999")).thenReturn(null);

        // 发送 GET 请求
        mockMvc.perform(get("/api/sys/logs/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));
    }

    // ============================== 根据标签查询 ==============================
    @Test
    public void testGetByTag_ValidTag() throws Exception {
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId("1");
        sysLogRecord.setTag("API");
        SysLogRecord sysLogRecord2 = new SysLogRecord();
        sysLogRecord2.setId("2");
        sysLogRecord2.setTag("API");
        // 准备测试数据
        List<SysLogRecord> logs = Arrays.asList(
                sysLogRecord,
                sysLogRecord2
        );
        when(sysLogRecordService.getByTag("API")).thenReturn(logs);

        // 发送 GET 请求
        mockMvc.perform(get("/api/sys/logs/tag/API"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ============================== 更新日志记录 ==============================
    @Test
    public void testUpdateLogRecord_Success() throws Exception {
        // 准备更新数据
        SysLogRecord requestLog = new SysLogRecord();
        requestLog.setTag("NEW_TAG");

        SysLogRecord updatedLog = new SysLogRecord();
        updatedLog.setId("1");
        updatedLog.setTag("NEW_TAG");
        updatedLog.setCreateDate(new Date());
        when(sysLogRecordService.updateById(any())).thenReturn(true);
        when(sysLogRecordService.getById("1")).thenReturn(updatedLog);

        // 发送 PUT 请求
        mockMvc.perform(put("/api/sys/logs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestLog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tag").value("NEW_TAG"));
    }

    // ============================== 批量删除 ==============================
    @Test
    public void testBatchDelete_ValidIds() throws Exception {
        // 准备批量删除的 ID 列表
        List<String> ids = Arrays.asList("1", "2", "3");
        when(sysLogRecordService.batchDelete(ids)).thenReturn(true);

        // 发送 DELETE 请求
        mockMvc.perform(delete("/api/sys/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sysLogRecordService, times(1)).batchDelete(ids);
    }

    // ============================== 工具方法：将对象转为 JSON 字符串 ==============================
    private String asJsonString(final Object obj) {
        try {
            return JacksonUtil.toJson(obj); // 假设 JacksonUtil 是项目中的 JSON 工具类
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}