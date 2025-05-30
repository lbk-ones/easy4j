package ej.spring.boot.starter.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface SysLogRecordMapper extends BaseMapper<SysLogRecord> {
}
