package easy4j.infra.log.operate;

import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = SysConstant.PARAM_PREFIX, name = "operate-log-enable", havingValue = "true")
public class OperateLogAutoConfiguration {

    @Bean
    public OperationLogAspect operationLogAspect(){
        return new OperationLogAspect();
    }

}
