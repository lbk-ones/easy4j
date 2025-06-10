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
package easy4j.infra.dbaccess.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.springframework.jdbc.datasource.init.ScriptUtils.*;

/**
 * DDLHelper
 *
 * @author bokun.li
 * @date 2025/6/9
 */
public class DDlHelper {

    /**
     * 单独抽取一个静态方法来执行sql语句，可以执行sql文件/字符串sql
     *
     * @author bokun.li
     * @date 2025/6/9
     */
    public static void execDDL(Connection connection, String ddlSql, List<String> sqlFiles, boolean isCloseConnection) throws IOException {
        try {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
            EncodedResource encodedResource = null;
            // exe sql text script
            if (StrUtil.isNotBlank(ddlSql)) {
                byte[] bytes = ddlSql.getBytes(StandardCharsets.UTF_8);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                InputStreamResource inputStreamResource = new InputStreamResource(byteArrayInputStream);
                encodedResource = new EncodedResource(inputStreamResource, StandardCharsets.UTF_8);
                executeSqlScript(connection, encodedResource, false, false, DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
                        DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
            }
            if (CollUtil.isEmpty(sqlFiles)) {
                return;
            }
            // exe sql file
            for (String sqlFile : sqlFiles) {
                File file = new File(sqlFile);
                if (file.exists()) {
                    FileInputStream fileReader;
                    fileReader = new FileInputStream(file);
                    InputStreamResource inputStreamResource = new InputStreamResource(fileReader);
                    encodedResource = new EncodedResource(inputStreamResource, StandardCharsets.UTF_8);
                } else {
                    ClassPathResource classPathResource = new ClassPathResource(sqlFile);
                    InputStream inputStream = classPathResource.getInputStream();
                    InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
                    encodedResource = new EncodedResource(inputStreamResource, StandardCharsets.UTF_8);
                }
                executeSqlScript(connection, encodedResource, false, false, DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
                        DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (isCloseConnection) {
                JdbcHelper.close(connection);
            }
        }
    }

}
