package easy4j.infra.flyway;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

public class ImportFilter implements AutoConfigurationImportFilter {


    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            matches[i] = !shouldExclude(autoConfigurationClasses[i]);
        }
        return matches;
    }

    private boolean shouldExclude(String autoConfigurationClass) {
        if (StrUtil.isNotBlank(autoConfigurationClass)) {
            boolean contains = autoConfigurationClass.contains("Flyway");
            if (contains) {
                return !Easy4j.getProperty(SysConstant.EASY4J_FLYWAY_ENABLE, boolean.class);
            }
        }
        return false;
    }
}