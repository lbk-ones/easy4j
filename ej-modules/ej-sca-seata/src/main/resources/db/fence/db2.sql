CREATE TABLE SYS_TCC_FENCE_LOG
(
    XID          VARCHAR(128) NOT NULL,
    BRANCH_ID    BIGINT       NOT NULL,
    ACTION_NAME  VARCHAR(64)  NOT NULL,
    STATUS       SMALLINT     NOT NULL,
    GMT_CREATE   TIMESTAMP(3) NOT NULL,
    GMT_MODIFIED TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (XID, BRANCH_ID)
);

-- 创建索引
CREATE INDEX IDX_GMT_MODIFIED ON SYS_TCC_FENCE_LOG (GMT_MODIFIED);
CREATE INDEX IDX_STATUS ON SYS_TCC_FENCE_LOG (STATUS);

-- 添加注释
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.XID IS 'global id';
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.BRANCH_ID IS 'branch id';
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.ACTION_NAME IS 'action name';
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.STATUS IS 'status(tried:1;committed:2;rollbacked:3;suspended:4)';
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.GMT_CREATE IS 'create time';
COMMENT ON COLUMN SYS_TCC_FENCE_LOG.GMT_MODIFIED IS 'update time';