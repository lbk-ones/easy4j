package easy4j.module.base.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SqlFileExecute {
    public static void executeSqlFile(JdbcTemplate jdbcTemplate, String filePath) {
        try {
            // 从类路径下读取 SQL 文件内容
            String sqlContent = readSqlFileFromClasspath(filePath);
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
                inSingleQuote =!inSingleQuote;
            } else if (c == '"') {
                inDoubleQuote =!inDoubleQuote;
            } else if (c == ';' &&!inSingleQuote &&!inDoubleQuote) {
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
}
