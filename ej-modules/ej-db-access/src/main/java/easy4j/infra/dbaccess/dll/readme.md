# 功能介绍
```text

- ddl语句自动生成，自动执行
1、根据java类来生成ddl语句
2、根据模型来生成ddl语句
3、执行自动建表逻辑，如果没有建过表执行，有如果有新增字段则执行新增字段逻辑（只新增不修改）
4、将数据库中的指定表结构集合copy出来
5、数据库ddl转换,将mysql,oracle,sqlserver,pg等数据库互转
6、数据库的schema信息查询（表信息，字段信息，索引信息）

- 动态表查询
传入schema、tableName、whereBuild(条件构造器)、dataSource，可进行表的动态查询
1、全部字段查询
2、指定字段查询
3、指定条件查询
4、分页查询

PS:
以上功能均支持多种数据库如，mysql、postgresql、h2、db2、oracle、sqlserver

```

# mysql 通用建表语法模板
```text
CREATE [TEMPORARY] TABLE [IF NOT EXISTS] 表名 (
    字段1 数据类型 [约束1] [约束2],
    字段2 数据类型 [约束1] [约束2],
    ...,
    [PRIMARY KEY (字段名1, ...)],
    [FOREIGN KEY (字段名) REFERENCES 关联表(关联字段) ON DELETE 操作 ON UPDATE 操作],
    [INDEX 索引名 (字段名1, ...)]
) COMMENT '表注释'
  ENGINE=存储引擎 
  DEFAULT CHARSET=字符集 
  [PARTITION BY ...];
```

# 标准 SQL 建表语法（无数据库特有扩展）

```text
CREATE TABLE 模式名.表名 (
    -- 字段定义（使用标准数据类型和约束）
    字段1 标准数据类型 NOT NULL,
    字段2 标准数据类型 NOT NULL,
    字段3 标准数据类型 NULL,
    字段4 标准数据类型 DEFAULT 标准默认值,
    字段5 标准数据类型 CHECK (字段5 > 0),  -- 列级检查约束
    
    -- 表级约束定义（标准语法）
    CONSTRAINT 主键约束名 PRIMARY KEY (字段1),
    CONSTRAINT 唯一约束名 UNIQUE (字段2),
    CONSTRAINT 检查约束名 CHECK (字段3 IN ('值1', '值2')),
    CONSTRAINT 外键约束名 FOREIGN KEY (字段4) 
        REFERENCES 关联表名(关联字段)
        ON DELETE CASCADE  -- 级联删除（标准支持的操作）
        ON UPDATE RESTRICT  -- 限制更新（标准支持的操作）
);

-- 标准 SQL 注释语法（表注释）
COMMENT ON TABLE 模式名.表名 IS '表的功能描述信息';

-- 标准 SQL 注释语法（字段注释）
COMMENT ON COLUMN 模式名.表名.字段1 IS '字段1的详细说明';
COMMENT ON COLUMN 模式名.表名.字段2 IS '字段2的详细说明';

``` 