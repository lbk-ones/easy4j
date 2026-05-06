create table if not exists sys_log_record
(
    id          varchar(500) primary key comment '主键',
    `tag`         varchar(100) comment '日志标签',
    tag_desc    varchar(200) comment '标签描述',
    trace_id    varchar(100) comment '链路id',
    status      varchar(50) comment '处理状态',
    process_time varchar(50) comment '处理时间',
    create_date datetime comment '操作时间(长文本)',
    params      text comment '参数(长文本)',
    remark      text comment '备注(长文本)',
    error_info  text comment '错误信息',
    target_id   varchar(200) comment '操作对象id(当前操作的对象标识id)',
    target_id2  varchar(200) comment '当前操作对象第二个标识id',
    operate_code varchar(300) comment '操作人代码',
    operate_name varchar(700) comment '操作人姓名',
    index idx_sys_log_record_create_date (create_date),
    index idx_sys_log_record_target_id (target_id),
    index idx_sys_log_record_tag (`tag`)
) engine = innodb
  default charset = utf8mb4 comment ='系统日志记录表';