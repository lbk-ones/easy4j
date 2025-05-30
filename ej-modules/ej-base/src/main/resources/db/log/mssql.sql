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
    ERROR_INFO   NVARCHAR(MAX),
    TARGET_ID    NVARCHAR(200),
    TARGET_ID2   NVARCHAR(200),
    OPERATE_CODE NVARCHAR(300),
    OPERATE_NAME NVARCHAR(700)
);
EXEC sp_addextendedproperty 'MS_Description', N'主键', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN', 'ID';
EXEC sp_addextendedproperty 'MS_Description', N'日志标签', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN', 'TAG';
EXEC sp_addextendedproperty 'MS_Description', N'标签描述', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'TAG_DESC';
EXEC sp_addextendedproperty 'MS_Description', N'链路ID', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'TRACE_ID';
EXEC sp_addextendedproperty 'MS_Description', N'处理状态', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'STATUS';
EXEC sp_addextendedproperty 'MS_Description', N'处理时间', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'PROCESS_TIME';
EXEC sp_addextendedproperty 'MS_Description', N'操作时间(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'CREATE_DATE';
EXEC sp_addextendedproperty 'MS_Description', N'参数(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'PARAMS';
EXEC sp_addextendedproperty 'MS_Description', N'备注(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'REMARK';
EXEC sp_addextendedproperty 'MS_Description', N'错误信息', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'ERROR_INFO';
EXEC sp_addextendedproperty 'MS_Description', N'操作对象ID(当前操作的对象标识id)', 'SCHEMA', 'dbo', 'TABLE',
     'SYS_LOG_RECORD', 'COLUMN',
     'TARGET_ID';
EXEC sp_addextendedproperty 'MS_Description', N'当前操作对象第二个标识id', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD',
     'COLUMN',
     'TARGET_ID2';
EXEC sp_addextendedproperty 'MS_Description', N'操作人代码', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'OPERATE_CODE';
EXEC sp_addextendedproperty 'MS_Description', N'操作人姓名', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOG_RECORD', 'COLUMN',
     'OPERATE_NAME';
CREATE INDEX IDX_SYS_LOG_RECORD_CREATE_DATE ON SYS_LOG_RECORD (CREATE_DATE);
CREATE INDEX IDX_SYS_LOG_RECORD_TARGET_ID ON SYS_LOG_RECORD (TARGET_ID);
CREATE INDEX IDX_SYS_LOG_RECORD_TAG ON SYS_LOG_RECORD (TAG);
