create table if not exists sys_tcc_fence_log
(
    xid          varchar(128) not null,
    branch_id    bigint       not null,
    action_name  varchar(64)  not null,
    status       tinyint      not null,
    gmt_create   timestamp(3) not null,
    gmt_modified timestamp(3) not null,
    primary key (xid, branch_id)
);

-- 创建索引
create index idx_gmt_modified on sys_tcc_fence_log (gmt_modified);
create index idx_status on sys_tcc_fence_log (status);

-- 添加注释（h2 1.4.199+ 支持）
comment on column sys_tcc_fence_log.xid is 'global id';
comment on column sys_tcc_fence_log.branch_id is 'branch id';
comment on column sys_tcc_fence_log.action_name is 'action name';
comment on column sys_tcc_fence_log.status is 'status(tried:1;committed:2;rollbacked:3;suspended:4)';
comment on column sys_tcc_fence_log.gmt_create is 'create time';
comment on column sys_tcc_fence_log.gmt_modified is 'update time';