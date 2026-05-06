create table sys_tcc_fence_log
(
    xid          varchar2(128) not null,
    branch_id    number(19)    not null,
    action_name  varchar2(64)  not null,
    status       number(3)     not null,
    gmt_create   timestamp(3)  not null,
    gmt_modified timestamp(3)  not null,
    primary key (xid, branch_id)
);
create index idx_gmt_modified on sys_tcc_fence_log (gmt_modified);
create index idx_status on sys_tcc_fence_log (status);