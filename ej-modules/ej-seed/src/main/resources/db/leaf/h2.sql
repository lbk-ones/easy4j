-- 创建 sys_leaf_alloc 表，用于存储业务相关分配信息
create table sys_leaf_alloc
(
    -- 业务key，唯一标识业务，长度为 128 字符，不能为空，默认值为空字符串
    biz_tag     varchar(128) not null default '' comment '业务key',
    -- 当前已经分配了的最大id，使用 bigint 存储
    max_id      bigint       not null default 1 comment '当前已经分配了的最大id',
    -- 初始步长，也是动态调整的最小步长
    step        int          not null comment '初始步长，也是动态调整的最小步长',
    -- 业务key的描述，长度为 256 字符
    description varchar(256)          default null comment '业务key的描述',
    -- 更新时间，使用 timestamp 类型
    update_time timestamp comment '更新时间',
    -- 定义主键约束
    constraint pk_sys_leaf_alloc primary key (biz_tag)
);