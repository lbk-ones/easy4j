package ej.spring.boot.starter.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;

import java.util.Date;
import java.util.List;

/**
 * SysLogRecord 服务接口
 */
public interface SysLogRecordService extends IService<SysLogRecord> {

    /**
     * 根据标签查询日志记录
     */
    List<SysLogRecord> getByTag(String tag);

    /**
     * 根据操作人代码查询日志记录
     */
    List<SysLogRecord> getByOperateCode(String operateCode);

    /**
     * 根据创建日期范围查询日志记录
     */
    List<SysLogRecord> getByCreateDateBetween(Date startDate, Date endDate);

    /**
     * 批量删除日志记录
     */
    boolean batchDelete(List<String> ids);

}