-- 创建 SYS_LEAF_ALLOC 表，用于存储业务相关分配信息
CREATE TABLE sys_leaf_alloc
(
    -- 业务key，唯一标识业务，长度为 128 字符，不能为空，默认值为空字符串
    biz_tag     VARCHAR(128) NOT NULL DEFAULT '',
    -- 当前已经分配了的最大id，使用 BIGINT 存储
    max_id      BIGINT       NOT NULL DEFAULT 1,
    -- 初始步长，也是动态调整的最小步长
    step        INT          NOT NULL,
    -- 业务key的描述，长度为 256 字符
    description VARCHAR(256)          DEFAULT NULL,
    -- 更新时间，使用 DATETIME 类型
    update_time DATETIME,
    -- 定义主键约束
    CONSTRAINT pk_sys_leaf_alloc PRIMARY KEY (biz_tag)
);

-- 为 BIZ_TAG 字段添加备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'业务key',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_leaf_alloc',
    @level2type = N'COLUMN', @level2name = 'biz_tag';

-- 为 MAX_ID 字段添加备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'当前已经分配了的最大id',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_leaf_alloc',
    @level2type = N'COLUMN', @level2name = 'max_id';

-- 为 STEP 字段添加备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'初始步长，也是动态调整的最小步长',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_leaf_alloc',
    @level2type = N'COLUMN', @level2name = 'step';

-- 为 DESCRIPTION 字段添加备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'业务key的描述',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_leaf_alloc',
    @level2type = N'COLUMN', @level2name = 'description';

-- 为 UPDATE_TIME 字段添加备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'更新时间',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_leaf_alloc',
    @level2type = N'COLUMN', @level2name = 'update_time';