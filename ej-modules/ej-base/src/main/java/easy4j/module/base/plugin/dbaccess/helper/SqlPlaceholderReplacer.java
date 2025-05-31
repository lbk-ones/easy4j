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
package easy4j.module.base.plugin.dbaccess.helper;

import easy4j.module.base.plugin.dbaccess.dialect.Dialect;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlPlaceholderReplacer
 *
 * @author bokun.li
 * @date 2025-05-31 23:25:19
 */
public class SqlPlaceholderReplacer {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 替换 SQL 中的 ? 占位符为具体参数值
     *
     * @param sql    带 ? 占位符的 SQL 语句
     * @param params 参数列表（支持 String、Integer、Long、Date、Timestamp 等类型）
     * @return 替换后的 SQL 语句
     */
    public static String replacePlaceholders(String sql, List<Object> params) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(sql);
        List<String> paramValues = new ArrayList<>();

        // 检查参数数量是否匹配
        int placeholderCount = 0;
        while (matcher.find()) {
            placeholderCount++;
        }
        if (params.size() != placeholderCount) {
            throw new IllegalArgumentException("参数数量与占位符数量不匹配");
        }

        // 重置 matcher 以重新匹配
        matcher = PLACEHOLDER_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();
        int index = 0;

        while (matcher.find()) {
            Object param = params.get(index++);
            String paramStr = convertParamToString(param);
            matcher.appendReplacement(result, Matcher.quoteReplacement(paramStr));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 将参数转换为 SQL 字符串形式
     */
    private static String convertParamToString(Object param) {
        Dialect dialectFromUrl = JdbcHelper.getDialectFromUrl();
        if (param == null) {
            return "NULL";
        } else if (param instanceof String) {
            return "'" + param.toString().replace("'", "''") + "'"; // 转义单引号
        } else if (param instanceof Date || param instanceof Timestamp) {

            String dateStr = DATE_FORMAT.format(param);
            return dialectFromUrl.strDateToFunc(dateStr); // 日期转为字符串字面量
        } else if (param instanceof Number || param instanceof Boolean) {
            return param.toString(); // 数值和布尔值直接转换
        } else {
            throw new IllegalArgumentException("不支持的参数类型: " + param.getClass());
        }
    }
}