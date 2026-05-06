create table if not exists sys_lock
(
    id           varchar(36) primary key comment '主键',
    create_date  datetime comment '操作时间',
    expire_date  datetime comment '过期时间',
    remark       datetime comment '备注',
    index idx_sys_lock_create_date (create_date)
) engine = innodb default charset = utf8mb4 comment ='系统锁';