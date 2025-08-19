package easy4j.infra.dbaccess.dynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL约束与注释构建器
 * 用于动态生成包含各种约束和注释的PostgreSQL建表语句
 */
public class PostgresConstraintBuilder {
    private final String tableName;
    private String tableComment;  // 表注释
    private final List<String> columns = new ArrayList<>();
    private final List<String> constraints = new ArrayList<>();
    // 存储列注释: 列名 -> 注释内容
    private final Map<String, String> columnComments = new HashMap<>();
    // 存储约束注释: 约束名 -> 注释内容
    private final Map<String, String> constraintComments = new HashMap<>();

    /**
     * 构造函数，初始化表名
     * @param tableName 表名
     */
    public PostgresConstraintBuilder(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 设置表注释
     * @param comment 表的描述信息
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder setTableComment(String comment) {
        this.tableComment = comment;
        return this;
    }

    /**
     * 添加列定义
     * @param columnName 列名
     * @param columnDefinition 列定义（包含数据类型和非空等约束）
     * @param comment 列注释（可为null，表示不添加注释）
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder addColumn(String columnName, String columnDefinition, String comment) {
        columns.add(columnDefinition);
        if (comment != null && !comment.isEmpty()) {
            columnComments.put(columnName, comment);
        }
        return this;
    }

    /**
     * 添加主键约束
     * @param constraintName 约束名称
     * @param comment 约束注释（可为null，表示不添加注释）
     * @param columnNames 主键列名数组
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder addPrimaryKey(String constraintName, String comment, String... columnNames) {
        String columnsStr = String.join(", ", columnNames);
        constraints.add(String.format("CONSTRAINT %s PRIMARY KEY (%s)", constraintName, columnsStr));
        if (comment != null && !comment.isEmpty()) {
            constraintComments.put(constraintName, comment);
        }
        return this;
    }

    /**
     * 添加外键约束
     * @param constraintName 约束名称
     * @param comment 约束注释（可为null，表示不添加注释）
     * @param sourceColumns 源表列名数组
     * @param targetTable 目标表名
     * @param targetColumns 目标表列名数组
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder addForeignKey(String constraintName, String comment,
                                                  String[] sourceColumns, String targetTable,
                                                  String[] targetColumns) {
        String sourceCols = String.join(", ", sourceColumns);
        String targetCols = String.join(", ", targetColumns);
        constraints.add(String.format(
            "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)",
            constraintName, sourceCols, targetTable, targetCols
        ));
        if (comment != null && !comment.isEmpty()) {
            constraintComments.put(constraintName, comment);
        }
        return this;
    }

    /**
     * 添加唯一约束
     * @param constraintName 约束名称
     * @param comment 约束注释（可为null，表示不添加注释）
     * @param columns 列名数组
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder addUniqueConstraint(String constraintName, String comment,
                                                       String[] columns) {
        String colsStr = String.join(", ", columns);
        constraints.add(String.format(
            "CONSTRAINT %s UNIQUE (%s)",
            constraintName, colsStr
        ));
        if (comment != null && !comment.isEmpty()) {
            constraintComments.put(constraintName, comment);
        }
        return this;
    }

    /**
     * 添加检查约束
     * @param constraintName 约束名称
     * @param comment 约束注释（可为null，表示不添加注释）
     * @param condition 检查条件
     * @return 当前构建器实例，支持链式调用
     */
    public PostgresConstraintBuilder addCheckConstraint(String constraintName, String comment,
                                                      String condition) {
        constraints.add(String.format(
            "CONSTRAINT %s CHECK (%s)",
            constraintName, condition
        ));
        if (comment != null && !comment.isEmpty()) {
            constraintComments.put(constraintName, comment);
        }
        return this;
    }

    /**
     * 构建完整的SQL语句，包括建表语句和所有注释语句
     * @return 完整的SQL脚本
     */
    public String build() {
        StringBuilder sql = new StringBuilder();
        
        // 构建CREATE TABLE语句
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");
        
        // 添加列定义
        sql.append("    ").append(String.join(",\n    ", columns));
        
        // 添加约束
        if (!constraints.isEmpty()) {
            sql.append(",\n    ").append(String.join(",\n    ", constraints));
        }
        
        sql.append("\n);\n\n");
        
        // 添加表注释
        if (tableComment != null && !tableComment.isEmpty()) {
            sql.append(String.format(
                "COMMENT ON TABLE %s IS '%s';\n\n",
                tableName, escapeQuotes(tableComment)
            ));
        }
        
        // 添加列注释
        for (Map.Entry<String, String> entry : columnComments.entrySet()) {
            sql.append(String.format(
                "COMMENT ON COLUMN %s.%s IS '%s';\n",
                tableName, entry.getKey(), escapeQuotes(entry.getValue())
            ));
        }
        if (!columnComments.isEmpty()) {
            sql.append("\n");
        }
        
        // 添加约束注释
        for (Map.Entry<String, String> entry : constraintComments.entrySet()) {
            sql.append(String.format(
                "COMMENT ON CONSTRAINT %s ON %s IS '%s';\n",
                entry.getKey(), tableName, escapeQuotes(entry.getValue())
            ));
        }
        
        return sql.toString();
    }
    
    /**
     * 转义单引号，防止SQL注入和语法错误
     * @param value 需要转义的字符串
     * @return 转义后的字符串
     */
    private String escapeQuotes(String value) {
        return value.replace("'", "''");
    }

    /**
     * 示例用法
     */
    public static void main(String[] args) {
        // 创建学生表构建器
        PostgresConstraintBuilder builder = new PostgresConstraintBuilder("students");
        
        // 设置表注释
        builder.setTableComment("学生信息表，存储所有学生的基本信息");
        
        // 添加列定义及列注释
        builder.addColumn("student_id", "student_id SERIAL", "学生ID，自增主键")
               .addColumn("name", "name VARCHAR(50) NOT NULL", "学生姓名，不能为空")
               .addColumn("student_no", "student_no VARCHAR(20)", "学号，学校内唯一")
               .addColumn("age", "age INT", "学生年龄")
               .addColumn("gender", "gender CHAR(1) DEFAULT '男'", "性别，默认为男")
               .addColumn("class_id", "class_id INT", "班级ID，关联班级表");
        
        // 添加各种约束及约束注释
        builder.addPrimaryKey("pk_student_id", "学生表主键，唯一标识学生记录", "student_id")
               .addUniqueConstraint("uk_student_no", "确保学号在学校内唯一", new String[]{"student_no"})
               .addCheckConstraint("chk_student_age", "限制学生年龄在6到25岁之间", "age BETWEEN 6 AND 25")
               .addForeignKey("fk_student_class", "关联班级表，确保学生所属班级存在",
                   new String[]{"class_id"}, 
                   "classes", 
                   new String[]{"class_id"});
        
        // 生成并打印SQL语句
        String createTableSql = builder.build();
        System.out.println(createTableSql);
    }
}
    