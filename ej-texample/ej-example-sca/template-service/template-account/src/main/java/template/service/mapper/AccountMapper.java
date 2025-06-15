package template.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import template.service.domains.Account;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
