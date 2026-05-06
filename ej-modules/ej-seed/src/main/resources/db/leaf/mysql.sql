create table sys_leaf_alloc
(
    biz_tag     varchar(128) not null default '' comment '业务key',
    max_id      bigint(20)   not null default '1' comment '当前已经分配了的最大id',
    step        int(11)      not null comment '初始步长，也是动态调整的最小步长',
    description varchar(256)          default null comment '业务key的描述',
    update_time timestamp    not null default current_timestamp on update current_timestamp comment '更新时间',
    primary key (biz_tag)
) engine = innodb;