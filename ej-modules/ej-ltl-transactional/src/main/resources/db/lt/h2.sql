-- 创建消息表
create table sys_local_message
(
    -- 消息 ID，唯一标识一条消息，作为主键
    msg_id       varchar(255) not null comment '消息 ID',
    -- 业务相关的唯一键，用于标识业务操作
    business_key varchar(255) comment '业务键',
    business_name varchar(255) comment '业务描述',
    -- 消息的具体内容，可存储任意长度的文本
    content clob comment '消息内容',
    -- 处理该消息所使用的 Bean 名称
    bean_name    varchar(255) comment 'Bean 名称',
    -- 处理该消息所调用的 Bean 方法
    bean_method  varchar(255) comment 'Bean 方法',
    -- 消息处理失败后的重试次数
    retry_count  int comment '重试次数',
    -- 消息当前的处理状态，使用整数表示不同状态
    status       int comment '消息状态',
    -- 消息记录创建的日期和时间
    create_date  timestamp comment '创建日期',
    -- 消息记录最后更新的日期和时间
    update_date  timestamp comment '更新日期',
    -- 消息处理过程中产生的错误信息
    error_message clob comment '错误消息',
    -- 标记消息是否被冻结，使用字符串表示冻结状态
    is_freeze    varchar(255) comment '是否冻结',
    -- 设置 MSG_ID 为主键
    constraint pk_sys_local_message primary key (msg_id)
);