create table sys_lock
(
    id          varchar(36) primary key comment '主键',
    create_date timestamp comment '操作时间',
    expire_date timestamp comment '过期时间',
    remark      varchar(36) comment '备注'
);
create index idx_sys_lock_create_date on sys_lock (create_date);