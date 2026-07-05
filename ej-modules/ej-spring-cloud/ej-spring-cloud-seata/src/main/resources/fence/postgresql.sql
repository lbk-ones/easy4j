-- -------------------------------- the script used for tcc fence  --------------------------------
create table if not exists public.sys_tcc_fence_log
(
    xid          varchar(128) not null,
    branch_id    bigint       not null,
    action_name  varchar(64)  not null,
    status       smallint     not null,
    gmt_create   timestamp(3) not null,
    gmt_modified timestamp(3) not null,
    constraint pk_sys_tcc_fence_log primary key (xid, branch_id)
);
create index idx_gmt_modified on public.sys_tcc_fence_log (gmt_modified);
create index idx_status on public.sys_tcc_fence_log (status);