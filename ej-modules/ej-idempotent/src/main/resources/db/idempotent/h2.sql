create table sys_key_idempotent
(
    ide_key   varchar(512) not null comment '业务key',
    expire_date timestamp    not null comment '过期时间',
    primary key (ide_key)
);