-- 创建消息表
create table sys_local_message
(
    -- 消息 ID，唯一标识一条消息，作为主键
    msg_id        varchar2(255) not null,
    -- 业务相关的唯一键，用于标识业务操作
    business_key  varchar2(255),
    business_name varchar2(255),
    -- 消息的具体内容，可存储任意长度的文本
    content       clob,
    -- 处理该消息所使用的 Bean 名称
    bean_name     varchar2(255),
    -- 处理该消息所调用的 Bean 方法
    bean_method   varchar2(255),
    -- 消息处理失败后的重试次数
    retry_count   number(10),
    -- 消息当前的处理状态，使用整数表示不同状态
    status        number(10),
    -- 消息记录创建的日期和时间
    create_date   timestamp,
    -- 消息记录最后更新的日期和时间
    update_date   timestamp,
    -- 消息处理过程中产生的错误信息
    error_message clob,
    -- 标记消息是否被冻结，使用字符串表示冻结状态
    is_freeze     varchar2(255),
    -- 设置 MSG_ID 为主键
    constraint pk_sys_local_message primary key (msg_id)
);

-- 添加字段注释
comment on column sys_local_message.msg_id is '消息 ID';
comment on column sys_local_message.business_key is '业务键';
comment on column sys_local_message.business_name is '业务描述';
comment on column sys_local_message.content is '消息内容';
comment on column sys_local_message.bean_name is 'Bean 名称';
comment on column sys_local_message.bean_method is 'Bean 方法';
comment on column sys_local_message.retry_count is '重试次数';
comment on column sys_local_message.status is '消息状态';
comment on column sys_local_message.create_date is '创建日期';
comment on column sys_local_message.update_date is '更新日期';
comment on column sys_local_message.error_message is '错误消息';
comment on column sys_local_message.is_freeze is '是否冻结';

-- 添加表注释
comment on table sys_local_message is '本地信息表';