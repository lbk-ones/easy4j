CREATE TABLE sys_tcc_fence_log
(
    xid          VARCHAR(128) NOT NULL,
    branch_id    BIGINT       NOT NULL,
    action_name  VARCHAR(64)  NOT NULL,
    status       TINYINT      NOT NULL,
    gmt_create   DATETIME2(3) NOT NULL,
    gmt_modified DATETIME2(3) NOT NULL,
    PRIMARY KEY CLUSTERED (xid, branch_id)
    );

CREATE NONCLUSTERED INDEX idx_gmt_modified ON sys_tcc_fence_log (gmt_modified);
CREATE NONCLUSTERED INDEX idx_status ON sys_tcc_fence_log (status);

EXEC sp_addextendedproperty
     @name = N'MS_Description',
     @value = N'GLOBAL ID',
     @level0type = N'SCHEMA', @level0name = N'dbo',
     @level1type = N'TABLE', @level1name = N'sys_tcc_fence_log',
     @level2type = N'COLUMN', @level2name = N'xid';