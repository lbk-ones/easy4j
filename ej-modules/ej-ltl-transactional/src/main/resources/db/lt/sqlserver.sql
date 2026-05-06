-- 创建消息表
CREATE TABLE sys_local_message
(
    -- 消息 ID，唯一标识一条消息，作为主键
    msg_id       NVARCHAR(255) NOT NULL,
    -- 业务相关的唯一键，用于标识业务操作
    business_key NVARCHAR(255),
    business_name NVARCHAR(255),
    -- 消息的具体内容，可存储任意长度的文本
    content      NVARCHAR(MAX) NOT NULL,
    -- 处理该消息所使用的 Bean 名称
    bean_name NVARCHAR(255),
    -- 处理该消息所调用的 Bean 方法
    bean_method NVARCHAR(255),
    -- 消息处理失败后的重试次数
    retry_count INT,
    -- 消息当前的处理状态，使用整数表示不同状态
    status INT,
    -- 消息记录创建的日期和时间
    create_date DATETIME,
    -- 消息记录最后更新的日期和时间
    update_date DATETIME,
    -- 消息处理过程中产生的错误信息
    error_message NVARCHAR(MAX),
    -- 标记消息是否被冻结，使用字符串表示冻结状态
    is_freeze NVARCHAR(255),
    -- 设置 MSG_ID 为主键
    CONSTRAINT pk_sys_local_message PRIMARY KEY (msg_id)
);

-- 添加字段注释
EXEC sp_addextendedproperty 'MS_Description', '消息 ID', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'msg_id';
EXEC sp_addextendedproperty 'MS_Description', '业务键', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'business_key';
EXEC sp_addextendedproperty 'MS_Description', '业务描述', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'business_name';
EXEC sp_addextendedproperty 'MS_Description', '消息内容', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'content';
EXEC sp_addextendedproperty 'MS_Description', 'Bean 名称', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'bean_name';
EXEC sp_addextendedproperty 'MS_Description', 'Bean 方法', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'bean_method';
EXEC sp_addextendedproperty 'MS_Description', '重试次数', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'retry_count';
EXEC sp_addextendedproperty 'MS_Description', '消息状态', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'status';
EXEC sp_addextendedproperty 'MS_Description', '创建日期', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'create_date';
EXEC sp_addextendedproperty 'MS_Description', '更新日期', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'update_date';
EXEC sp_addextendedproperty 'MS_Description', '错误消息', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'error_message';
EXEC sp_addextendedproperty 'MS_Description', '是否冻结', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message', 'COLUMN', 'is_freeze';

-- 添加表注释
EXEC sp_addextendedproperty 'MS_Description', '本地信息表', 'SCHEMA', 'dbo', 'TABLE', 'sys_local_message';