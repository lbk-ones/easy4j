package ej.spring.boot.starter.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * SysLogRecord MyBatis-Plus Mapper接口
 */
@Mapper
public interface SysLogRecordMapper extends BaseMapper<SysLogRecord> {

    /**
     * 根据标签查询日志记录
     */
    List<SysLogRecord> selectByTag(String tag);

    /**
     * 根据操作人代码查询日志记录
     */
    List<SysLogRecord> selectByOperateCode(String operateCode);

    /**
     * 根据创建日期范围查询日志记录
     */
    List<SysLogRecord> selectByCreateDateBetween(Date startDate, Date endDate);
}    