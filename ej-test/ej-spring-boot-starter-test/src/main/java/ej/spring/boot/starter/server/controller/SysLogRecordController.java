package ej.spring.boot.starter.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import ej.spring.boot.starter.server.service.SysLogRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SysLogRecord RESTful API 控制器
 */
@RestController
@RequestMapping("/api/sys/logs")
public class SysLogRecordController {

    @Autowired
    private SysLogRecordService sysLogRecordService;

    /**
     * 创建日志记录
     */
    @PostMapping
    public Map<String, Object> create(@RequestBody SysLogRecord logRecord) {
        Easy4j.info("测试一下" + JacksonUtil.toJson(logRecord));
        logRecord.setCreateDate(new Date()); // 设置创建时间
        boolean success = sysLogRecordService.save(logRecord);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("data", logRecord);
        return result;
    }

    /**
     * 获取所有日志记录（分页）
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysLogRecord> page = new Page<>(pageNum, pageSize);
        IPage<SysLogRecord> pageResult = sysLogRecordService.page(page,
                new LambdaQueryWrapper<SysLogRecord>().orderByDesc(SysLogRecord::getCreateDate));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", pageResult.getTotal());
        result.put("data", pageResult.getRecords());
        return result;
    }

    /**
     * 根据ID获取日志记录
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable String id) {
        SysLogRecord logRecord = sysLogRecordService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", logRecord != null);
        result.put("data", logRecord);
        return result;
    }

    /**
     * 根据标签获取日志记录
     */
    @GetMapping("/tag/{tag}")
    public Map<String, Object> getByTag(@PathVariable String tag) {
        List<SysLogRecord> logs = sysLogRecordService.getByTag(tag);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", logs);
        return result;
    }

    /**
     * 根据操作人代码获取日志记录
     */
    @GetMapping("/operator/{code}")
    public Map<String, Object> getByOperateCode(@PathVariable String code) {
        List<SysLogRecord> logs = sysLogRecordService.getByOperateCode(code);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", logs);
        return result;
    }

    /**
     * 更新日志记录
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable String id, @RequestBody SysLogRecord logRecord) {
        logRecord.setId(id);
        boolean success = sysLogRecordService.updateById(logRecord);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("data", sysLogRecordService.getById(id));
        return result;
    }

    /**
     * 删除日志记录
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable String id) {
        boolean success = sysLogRecordService.removeById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    /**
     * 批量删除日志记录
     */
    @DeleteMapping
    public Map<String, Object> batchDelete(@RequestBody List<String> ids) {
        boolean success = sysLogRecordService.batchDelete(ids);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }
}    