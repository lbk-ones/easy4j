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
package easy4j.infra.dbaccess.dynamic.schema;

import easy4j.infra.dbaccess.DBAccess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AbstractDyInformationSchema
 *
 * @author bokun.li
 * @date 2025-07-31 19:56:25
 */
public abstract class AbstractDyInformationSchema implements DyInformationSchema {

    @Override
    public String getVersion() {
        try {
            return extractVersion(getDbAccess().selectScalar("select version()", String.class));
        } catch (Exception e) {
            return "-1";
        }
    }


    /**
     * 从字符串中提取PostgreSQL版本号
     *
     * @param input 包含版本信息的字符串
     * @return 提取到的版本号，若未找到则返回null
     */
    protected String extractVersion(String input) {
        // 正则表达式1-\d+\.\d+ 正则表达式说明：
        // \b 表示单词边界，确保匹配完整的版本号
        // \d+ 匹配1个或多个数字
        // \. 匹配点号
        Pattern pattern = java.util.regex.Pattern.compile("\\b\\d+\\.\\d+\\b");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}