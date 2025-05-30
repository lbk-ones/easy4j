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
package ej.spring.boot.starter.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import easy4j.module.base.header.EasyResult;
import easy4j.module.base.log.RequestLog;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.utils.json.JacksonUtil;
import ej.spring.boot.starter.server.mapper.SysLogRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TestController
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@RestController
public class TestController {


    @Autowired
    SysLogRecordMapper sysLogRecordMapper;

    @RequestMapping("hello")
    @RequestLog(tag = "test", tagDesc = "hello-test")
    public EasyResult<String> getEasyResult() {

        log.info("this a test hello~~");
        return EasyResult.ok("hello wolrd");
    }

    @RequestMapping("querySysLogRecord")
    public EasyResult<String> querySysLogRecord() {
        LambdaQueryWrapper<SysLogRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<SysLogRecord> sysLogRecords = sysLogRecordMapper.selectList(lambdaQueryWrapper);
        log.info("this a test hello~~");
        return EasyResult.ok(JacksonUtil.toJson(sysLogRecords));
    }
}
