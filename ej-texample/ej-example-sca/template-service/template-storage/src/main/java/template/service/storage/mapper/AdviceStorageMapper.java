package template.service.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import template.service.storage.domains.AdviceStorage;

@Mapper
public interface AdviceStorageMapper extends BaseMapper<AdviceStorage> {
}