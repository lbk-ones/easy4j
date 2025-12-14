create TABLE SYS_LOCK
(
    ID          NVARCHAR(36) PRIMARY KEY,
    CREATE_DATE DATETIME2,
    EXPIRE_DATE DATETIME2,
    REMARK      NVARCHAR(500)
);
EXEC sp_addextendedproperty 'MS_Description', '主键', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCK', 'COLUMN', 'ID';
EXEC sp_addextendedproperty 'MS_Description', '操作时间', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCK', 'COLUMN',
     'CREATE_DATE';
EXEC sp_addextendedproperty 'MS_Description', '过期时间', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCK', 'COLUMN',
     'EXPIRE_DATE';
EXEC sp_addextendedproperty 'MS_Description', '备注', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCK', 'COLUMN',
     'REMARK';
CREATE INDEX IDX_SYS_LOCK_CREATE_DATE ON SYS_LOCK (CREATE_DATE);
