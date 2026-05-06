create TABLE sys_lock
(
    id          NVARCHAR(36) PRIMARY KEY,
    create_date DATETIME2,
    expire_date DATETIME2,
    remark      NVARCHAR(500)
);
EXEC sp_addextendedproperty 'MS_Description', '主键', 'SCHEMA', 'dbo', 'TABLE', 'sys_lock', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', '操作时间', 'SCHEMA', 'dbo', 'TABLE', 'sys_lock', 'COLUMN',
     'create_date';
EXEC sp_addextendedproperty 'MS_Description', '过期时间', 'SCHEMA', 'dbo', 'TABLE', 'sys_lock', 'COLUMN',
     'expire_date';
EXEC sp_addextendedproperty 'MS_Description', '备注', 'SCHEMA', 'dbo', 'TABLE', 'sys_lock', 'COLUMN',
     'remark';
CREATE INDEX idx_sys_lock_create_date ON sys_lock (create_date);