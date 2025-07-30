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
package easy4j.infra.flyway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;

/**
 * 重写部分flyway执行逻辑
 *
 * @author bokun.li
 * @date 2025-07-28
 */
public class Easy4jFlywayMigrationStrategy implements FlywayMigrationStrategy {

    @Override
    public void migrate(Flyway flyway) {
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            if (e instanceof FlywayValidateException) {
                // default disabled content check
                boolean checkSumDisabled = Easy4j.getProperty(SysConstant.EASY4J_FLYWAY_CHECKSUM_DISABLED, boolean.class, true);
                // only server can throw checksum exception
                if (!SystemUtil.getOsInfo().isLinux() || checkSumDisabled) {
                    String message = e.getMessage();
                    if (StrUtil.contains(message, "Migration checksum mismatch")) {
                        Easy4j.error(SysLog.compact("flyway localhost not checksum"));
                        return;
                    }
                }
            }
            throw e;
        }
    }
}
