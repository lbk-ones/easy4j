create table sys_log_record
(
    id           varchar(500) primary key,
    tag          varchar(100),
    tag_desc     varchar(200),
    trace_id     varchar(100),
    status       varchar(50),
    process_time varchar(50),
    create_date  timestamp,
    params       text,
    remark       text,
    error_info   text,
    target_id    varchar(200),
    target_id2   varchar(200),
    operate_code varchar(300),
    operate_name varchar(700)
);
comment on table sys_log_record is '系统日志记录表';
comment on column sys_log_record.id is '主键';
comment on column sys_log_record.tag is '日志标签';
comment on column sys_log_record.tag_desc is '标签描述';
comment on column sys_log_record.trace_id is '链路id';
comment on column sys_log_record.status is '处理状态';
comment on column sys_log_record.process_time is '处理时间';
comment on column sys_log_record.create_date is '操作时间(长文本)';
comment on column sys_log_record.params is '参数(长文本)';
comment on column sys_log_record.remark is '备注(长文本)';
comment on column sys_log_record.error_info is '错误信息';
comment on column sys_log_record.target_id is '操作对象id(当前操作的对象标识id)';
comment on column sys_log_record.target_id2 is '当前操作对象第二个标识id';
comment on column sys_log_record.operate_code is '操作人代码';
comment on column sys_log_record.operate_name is '操作人姓名';

create index idx_sys_log_record_create_date on sys_log_record (create_date);
create index idx_sys_log_record_target_id on sys_log_record (target_id);
create index idx_sys_log_record_tag on sys_log_record (tag);