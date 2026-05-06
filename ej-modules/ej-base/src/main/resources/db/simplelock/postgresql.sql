create table sys_lock
(
    id          varchar(36) primary key,
    create_date timestamp,
    expire_date timestamp,
    remark      varchar(500)
);
comment on table sys_lock is '系统锁';
comment on column sys_lock.id is '主键';
comment on column sys_lock.create_date is '操作时间';
comment on column sys_lock.expire_date is '过期时间';
comment on column sys_lock.remark is '备注';

create index idx_sys_lock_create_date on sys_lock (create_date);