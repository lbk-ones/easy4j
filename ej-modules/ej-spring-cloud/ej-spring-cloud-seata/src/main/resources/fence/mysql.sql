-- -------------------------------- THE SCRIPT USE TCC FENCE  --------------------------------
create table if not exists sys_tcc_fence_log
(
    xid          varchar(128) not null comment 'global id',
    branch_id    bigint       not null comment 'branch id',
    action_name  varchar(64)  not null comment 'action name',
    status       tinyint      not null comment 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
    gmt_create   datetime(3)  not null comment 'create time',
    gmt_modified datetime(3)  not null comment 'update time',
    primary key (xid, branch_id),
    key idx_gmt_modified (gmt_modified),
    key idx_status (status)
) engine = innodb
  default charset = utf8mb4;