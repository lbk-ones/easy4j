-- 创建表
CREATE TABLE WORK_IP
(
    IP  VARCHAR(100),
    NUM INT,
    CONSTRAINT PK_WORK_IP PRIMARY KEY (IP)
);

-- 添加表注释
EXEC sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'分布式主键IP记录',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'WORK_IP';