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
package easy4j.module.base.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 不用它暂时废弃
 */
@Deprecated
public class SqlFileExecute {
    public static void executeSqlFile(JdbcTemplate jdbcTemplate, String filePath) {
        try {
            // 从类路径下读取 SQL 文件内容
            String sqlContent = readSqlFileFromClasspath(filePath);
            sqlContent = removeMultiLineComments(sqlContent);
            // 拆分 SQL 语句
            List<String> sqlStatements = splitSqlStatements(sqlContent);

            // 遍历并执行 SQL 语句
            for (String sql : sqlStatements) {
                if (!sql.trim().isEmpty()) {
                    jdbcTemplate.execute(sql);
                }
            }
        } catch (IOException e) {
            System.err.println("读取 SQL 文件时出错: " + e.getMessage());
        }
    }

    private static String readSqlFileFromClasspath(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        // 使用类加载器获取类路径下的资源输入流
        InputStream inputStream = SqlFileExecute.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException("未找到类路径下的文件: " + filePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StrUtil.trim(line).startsWith("--")) {
                    continue;
                }
                // 去除行尾注释
                line = removeComments(line);
                content.append(line).append(" "); // 用空格连接每行内容，避免换行影响语句
            }
        }
        return content.toString();
    }

    private static String removeComments(String line) {
        int commentIndex = line.indexOf("--");
        if (commentIndex != -1) {
            return line.substring(0, commentIndex).trim();
        }
        return line;
    }

    private static List<String> splitSqlStatements(String sqlContent) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < sqlContent.length(); i++) {
            char c = sqlContent.charAt(i);
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"') {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                statements.add(currentStatement.toString().trim());
                currentStatement.setLength(0);
                continue;
            }
            currentStatement.append(c);
        }

        if (currentStatement.length() > 0) {
            statements.add(currentStatement.toString().trim());
        }
        return statements;
    }


    // 去掉多行注释可能不太好 oracle有些语法是注释来完成的 但是不去掉可能会报错 so... 去掉吧
    public static String removeMultiLineComments(String input) {
        // 定义正则表达式，用于匹配 /** ... */ 形式的注释
        String regex = "/\\*.*?\\*/";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(input);
        // 将匹配到的注释替换为空字符串
        return matcher.replaceAll("");
    }
}
