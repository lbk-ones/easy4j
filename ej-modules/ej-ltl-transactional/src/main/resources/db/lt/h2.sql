-- 创建消息表
CREATE TABLE SYS_LOCAL_MESSAGE
(
    -- 消息 ID，唯一标识一条消息，作为主键
    MSG_ID       VARCHAR(255) NOT NULL COMMENT '消息 ID',
    -- 业务相关的唯一键，用于标识业务操作
    BUSINESS_KEY VARCHAR(255) COMMENT '业务键',
    BUSINESS_NAME VARCHAR(255) COMMENT '业务描述',
    -- 消息的具体内容，可存储任意长度的文本
    CONTENT CLOB COMMENT '消息内容',
    -- 处理该消息所使用的 Bean 名称
    BEAN_NAME    VARCHAR(255) COMMENT 'Bean 名称',
    -- 处理该消息所调用的 Bean 方法
    BEAN_METHOD  VARCHAR(255) COMMENT 'Bean 方法',
    -- 消息处理失败后的重试次数
    RETRY_COUNT  INT COMMENT '重试次数',
    -- 消息当前的处理状态，使用整数表示不同状态
    STATUS       INT COMMENT '消息状态',
    -- 消息记录创建的日期和时间
    CREATE_DATE  TIMESTAMP COMMENT '创建日期',
    -- 消息记录最后更新的日期和时间
    UPDATE_DATE  TIMESTAMP COMMENT '更新日期',
    -- 消息处理过程中产生的错误信息
    ERROR_MESSAGE CLOB COMMENT '错误消息',
    -- 标记消息是否被冻结，使用字符串表示冻结状态
    IS_FREEZE    VARCHAR(255) COMMENT '是否冻结',
    -- 设置 MSG_ID 为主键
    CONSTRAINT PK_SYS_LOCAL_MESSAGE PRIMARY KEY (MSG_ID)
);