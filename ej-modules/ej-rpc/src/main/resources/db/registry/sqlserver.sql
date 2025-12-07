-- 注册表 sys_e4j_jdbc_reg_data
CREATE TABLE sys_e4j_jdbc_reg_data
(
    id               BIGINT IDENTITY (1,1) PRIMARY KEY,
    data_key         NVARCHAR(2000),
    data_value       NVARCHAR(MAX),
    data_type        NVARCHAR(1),
    create_date      DATETIME2,
    last_update_date DATETIME2
);

-- 表注释
EXEC sp_addextendedproperty
     @name = N'MS_Description',
     @value = N'注册表',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data;

-- 字段注释
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'主键 自增',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = id;

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'键值key',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = data_key;

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'键值value',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = data_value;

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'数据类型 临时节点 0，存储节点 1',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = data_type;

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'创建时间',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = create_date;

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = N'更新时间',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_e4j_jdbc_reg_data,
     @level2type = N'COLUMN', @level2name = last_update_date;

-- 给data_key创建索引
CREATE INDEX idx_sys_e4j_jdbc_reg_data_data_key ON sys_e4j_jdbc_reg_data (data_key);

-- 给data_type创建索引
CREATE INDEX idx_sys_e4j_jdbc_reg_data_data_type ON sys_e4j_jdbc_reg_data (data_type);