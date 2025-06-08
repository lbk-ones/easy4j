package ej.spring.boot.starter.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import ej.spring.boot.starter.server.mapper.SysLogRecordMapper;
import ej.spring.boot.starter.server.service.SysLogRecordService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * SysLogRecord 服务实现类
 */
@Service
public class SysLogRecordServiceImpl extends ServiceImpl<SysLogRecordMapper, SysLogRecord> implements SysLogRecordService {

    @Override
    public List<SysLogRecord> getByTag(String tag) {
        return baseMapper.selectByTag(tag);
    }

    @Override
    public List<SysLogRecord> getByOperateCode(String operateCode) {
        return baseMapper.selectByOperateCode(operateCode);
    }

    @Override
    public List<SysLogRecord> getByCreateDateBetween(Date startDate, Date endDate) {
        return baseMapper.selectByCreateDateBetween(startDate, endDate);
    }

    @Override
    public boolean batchDelete(List<String> ids) {
        return removeByIds(ids);
    }
}    