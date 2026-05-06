create TABLE sys_log_record
(
    id           NVARCHAR(500) PRIMARY KEY,
    tag          NVARCHAR(100),
    tag_desc     NVARCHAR(200),
    trace_id     NVARCHAR(100),
    status       NVARCHAR(50),
    process_time NVARCHAR(50),
    create_date  DATETIME2,
    params       NVARCHAR(MAX),
    remark       NVARCHAR(MAX),
    error_info   NVARCHAR(MAX),
    target_id    NVARCHAR(200),
    target_id2   NVARCHAR(200),
    operate_code NVARCHAR(300),
    operate_name NVARCHAR(700)
);
EXEC sp_addextendedproperty 'MS_Description', N'主键', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN', 'id';
EXEC sp_addextendedproperty 'MS_Description', N'日志标签', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN', 'tag';
EXEC sp_addextendedproperty 'MS_Description', N'标签描述', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'tag_desc';
EXEC sp_addextendedproperty 'MS_Description', N'链路ID', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'trace_id';
EXEC sp_addextendedproperty 'MS_Description', N'处理状态', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'status';
EXEC sp_addextendedproperty 'MS_Description', N'处理时间', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'process_time';
EXEC sp_addextendedproperty 'MS_Description', N'操作时间(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'create_date';
EXEC sp_addextendedproperty 'MS_Description', N'参数(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'params';
EXEC sp_addextendedproperty 'MS_Description', N'备注(长文本)', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'remark';
EXEC sp_addextendedproperty 'MS_Description', N'错误信息', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'error_info';
EXEC sp_addextendedproperty 'MS_Description', N'操作对象ID(当前操作的对象标识id)', 'SCHEMA', 'dbo', 'TABLE',
     'sys_log_record', 'COLUMN',
     'target_id';
EXEC sp_addextendedproperty 'MS_Description', N'当前操作对象第二个标识id', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record',
     'COLUMN',
     'target_id2';
EXEC sp_addextendedproperty 'MS_Description', N'操作人代码', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'operate_code';
EXEC sp_addextendedproperty 'MS_Description', N'操作人姓名', 'SCHEMA', 'dbo', 'TABLE', 'sys_log_record', 'COLUMN',
     'operate_name';
CREATE INDEX idx_sys_log_record_create_date ON sys_log_record (create_date);
CREATE INDEX idx_sys_log_record_target_id ON sys_log_record (target_id);
CREATE INDEX idx_sys_log_record_tag ON sys_log_record (tag);