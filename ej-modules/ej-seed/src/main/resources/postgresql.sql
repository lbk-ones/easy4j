-- 创建 LEAF_ALLOC 表，用于存储业务相关分配信息
CREATE TABLE LEAF_ALLOC
(
    -- 业务key，唯一标识业务，长度为 128 字符，不能为空，默认值为空字符串
    BIZ_TAG     VARCHAR(128) NOT NULL DEFAULT '',
    -- 当前已经分配了的最大id，使用 BIGINT 存储
    MAX_ID      BIGINT       NOT NULL DEFAULT 1,
    -- 初始步长，也是动态调整的最小步长
    STEP        INT          NOT NULL,
    -- 业务key的描述，长度为 256 字符
    DESCRIPTION VARCHAR(256)          DEFAULT NULL,
    -- 更新时间，使用 TIMESTAMP 类型
    UPDATE_TIME TIMESTAMP,
    -- 定义主键约束
    CONSTRAINT PK_LEAF_ALLOC PRIMARY KEY (BIZ_TAG)
);

-- 为 BIZ_TAG 字段添加备注
COMMENT ON COLUMN LEAF_ALLOC.BIZ_TAG IS '业务key';
-- 为 MAX_ID 字段添加备注
COMMENT ON COLUMN LEAF_ALLOC.MAX_ID IS '当前已经分配了的最大id';
-- 为 STEP 字段添加备注
COMMENT ON COLUMN LEAF_ALLOC.STEP IS '初始步长，也是动态调整的最小步长';
-- 为 DESCRIPTION 字段添加备注
COMMENT ON COLUMN LEAF_ALLOC.DESCRIPTION IS '业务key的描述';
-- 为 UPDATE_TIME 字段添加备注
COMMENT ON COLUMN LEAF_ALLOC.UPDATE_TIME IS '更新时间';