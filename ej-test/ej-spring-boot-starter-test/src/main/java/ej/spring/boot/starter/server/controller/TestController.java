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
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import easy4j.infra.log.RequestLog;
import easy4j.module.idempotent.WebIdempotent;
import easy4j.module.seed.CommonKey;
import easy4j.module.sentinel.annotation.FlowDegradeResource;
import ej.spring.boot.starter.server.mapper.SysLogRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
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

    @Resource
    DBAccess dbAccess;

    @Autowired
    SysLogRecordMapper sysLogRecordMapper;

    @RequestMapping("hello")
    @RequestLog(tag = "test", tagDesc = "hello-test")
    public EasyResult<String> getEasyResult() {

        log.info("this a test hello~~");
        return EasyResult.ok("hello wolrd");
    }

    @PostMapping("saveLog")
    @RequestLog
    @WebIdempotent
    @FlowDegradeResource(value = "testSaveLog", flowCount = 50)
    public EasyResult<SysLogRecord> saveLog(@RequestBody SysLogRecord sysLogRecord) {

        sysLogRecord.setId(CommonKey.gennerString());
        sysLogRecord.setCreateDate(new Date());
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);

        log.info("save success effect rows:" + i);
        SysLogRecord sysLogRecord1 = dbAccess.selectByPrimaryKey(sysLogRecord, SysLogRecord.class);


        return EasyResult.ok(sysLogRecord1);
    }

    @RequestMapping("querySysLogRecord")
    public EasyResult<String> querySysLogRecord() {
        LambdaQueryWrapper<SysLogRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<SysLogRecord> sysLogRecords = sysLogRecordMapper.selectList(lambdaQueryWrapper);
        log.info("this a test hello~~");
        return EasyResult.ok(JacksonUtil.toJson(sysLogRecords));
    }


    @CachePut(cacheNames = SysConstant.PARAM_PREFIX, key = "#name")
    @RequestMapping("/cache/{name}")
    public EasyResult<List<SysLogRecord>> querySysLogRecord(@PathVariable(name = "name") String name) {
        LambdaQueryWrapper<SysLogRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<SysLogRecord> list = getList(5);
        int i = dbAccess.saveList(list, SysLogRecord.class);
        return EasyResult.ok(list);
    }

    List<SysLogRecord> getList(int i) {
        List<SysLogRecord> list = ListTs.newArrayList();
        for (int j = 0; j < i; j++) {
            String s = CommonKey.gennerString();
            SysLogRecord sysLogRecord = new SysLogRecord();
            sysLogRecord.setId(s);
            sysLogRecord.setStatus("1");
            sysLogRecord.setRemark("remark test" + j);
            sysLogRecord.setTag("tag test" + j);
            sysLogRecord.setParams("params test" + j);
            sysLogRecord.setTagDesc("tagDesc test" + j);
            sysLogRecord.setCreateDate(new Date());
            sysLogRecord.setTraceId("traceId" + j);
            sysLogRecord.setProcessTime(String.valueOf(j * 2));
            sysLogRecord.setErrorInfo("error into test" + j);
            sysLogRecord.setOperateCode("operate code " + j);
            sysLogRecord.setOperateName("operate name" + j);
            sysLogRecord.setTargetId("target id" + j);
            sysLogRecord.setTargetId2("target id2" + j);
            list.add(sysLogRecord);
        }
        return list;
    }
}
