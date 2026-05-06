create table sys_key_idempotent
(
    ide_key   varchar(128) not null,
    expire_date timestamp    not null,
    constraint sys_key_idempotent_pk primary key (ide_key)
);

-- 添加字段备注
comment on column sys_key_idempotent.ide_key is '业务key';
comment on column sys_key_idempotent.expire_date is '过期时间';