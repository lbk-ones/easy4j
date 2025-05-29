-- 创建消息表
CREATE TABLE SYS_LOCAL_MESSAGE
(
    -- 消息 ID，唯一标识一条消息，作为主键
    MSG_ID       NVARCHAR(255) NOT NULL,
    -- 业务相关的唯一键，用于标识业务操作
    BUSINESS_KEY NVARCHAR(255),
    BUSINESS_NAME NVARCHAR(255),
    -- 消息的具体内容，可存储任意长度的文本
    CONTENT      NVARCHAR(MAX) NOT NULL,
    -- 处理该消息所使用的 Bean 名称
    BEAN_NAME NVARCHAR(255),
    -- 处理该消息所调用的 Bean 方法
    BEAN_METHOD NVARCHAR(255),
    -- 消息处理失败后的重试次数
    RETRY_COUNT INT,
    -- 消息当前的处理状态，使用整数表示不同状态
    STATUS INT,
    -- 消息记录创建的日期和时间
    CREATE_DATE DATETIME,
    -- 消息记录最后更新的日期和时间
    UPDATE_DATE DATETIME,
    -- 消息处理过程中产生的错误信息
    ERROR_MESSAGE NVARCHAR(MAX),
    -- 标记消息是否被冻结，使用字符串表示冻结状态
    IS_FREEZE NVARCHAR(255),
    -- 设置 MSG_ID 为主键
    CONSTRAINT PK_SYS_LOCAL_MESSAGE PRIMARY KEY (MSG_ID)
);

-- 添加字段注释
EXEC sp_addextendedproperty 'MS_Description', '消息 ID', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'MSG_ID';
EXEC sp_addextendedproperty 'MS_Description', '业务键', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'BUSINESS_KEY';
EXEC sp_addextendedproperty 'MS_Description', '业务描述', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'BUSINESS_NAME';
EXEC sp_addextendedproperty 'MS_Description', '消息内容', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'CONTENT';
EXEC sp_addextendedproperty 'MS_Description', 'Bean 名称', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'BEAN_NAME';
EXEC sp_addextendedproperty 'MS_Description', 'Bean 方法', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'BEAN_METHOD';
EXEC sp_addextendedproperty 'MS_Description', '重试次数', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'RETRY_COUNT';
EXEC sp_addextendedproperty 'MS_Description', '消息状态', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'STATUS';
EXEC sp_addextendedproperty 'MS_Description', '创建日期', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'CREATE_DATE';
EXEC sp_addextendedproperty 'MS_Description', '更新日期', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'UPDATE_DATE';
EXEC sp_addextendedproperty 'MS_Description', '错误消息', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'ERROR_MESSAGE';
EXEC sp_addextendedproperty 'MS_Description', '是否冻结', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE', 'COLUMN', 'IS_FREEZE';

-- 添加表注释
EXEC sp_addextendedproperty 'MS_Description', '本地信息表', 'SCHEMA', 'dbo', 'TABLE', 'SYS_LOCAL_MESSAGE';