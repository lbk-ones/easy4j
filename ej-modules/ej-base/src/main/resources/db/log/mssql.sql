create TABLE SYS_LOG_RECORD
(
    ID           NVARCHAR(36) PRIMARY KEY,
    TAG          NVARCHAR(100),
    TAG_DESC     NVARCHAR(200),
    TRACE_ID     NVARCHAR(100),
    STATUS       NVARCHAR(50),
    PROCESS_TIME NVARCHAR(50),
    CREATE_DATE  DATETIME2,
    PARAMS       NVARCHAR(MAX),
    REMARK       NVARCHAR(MAX),
    ERROR_INFO   NVARCHAR(MAX)
);
EXEC sp_addextendedproperty 'MS_Description', '主键', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN', 'ID';
EXEC sp_addextendedproperty 'MS_Description', '日志标签', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN', 'TAG';
EXEC sp_addextendedproperty 'MS_Description', '标签描述', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'TAG_DESC';
EXEC sp_addextendedproperty 'MS_Description', '链路ID', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'TRACE_ID';
EXEC sp_addextendedproperty 'MS_Description', '处理状态', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'STATUS';
EXEC sp_addextendedproperty 'MS_Description', '处理时间', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'PROCESS_TIME';
EXEC sp_addextendedproperty 'MS_Description', '操作时间(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'CREATE_DATE';
EXEC sp_addextendedproperty 'MS_Description', '参数(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'PARAMS';
EXEC sp_addextendedproperty 'MS_Description', '备注(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'REMARK';
EXEC sp_addextendedproperty 'MS_Description', '错误信息', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'ERROR_INFO';
CREATE INDEX IDX_SYS_LOG_RECORD_CREATE_DATE ON SYS_LOG_RECORD (CREATE_DATE);
