-- 创建 sys_leaf_alloc 表，用于存储业务相关分配信息
create table sys_leaf_alloc
(
    -- 业务key，唯一标识业务，长度为 128 字符，不能为空
    biz_tag     varchar2(128) not null,
    -- 当前已经分配了的最大id，使用 number(20) 存储
    max_id      number(20)    not null,
    -- 初始步长，也是动态调整的最小步长
    step        number(11)    not null,
    -- 业务key的描述，长度为 256 字符
    description varchar2(256),
    -- 更新时间，使用 timestamp 类型
    update_time timestamp
);

-- 为 biz_tag 字段添加备注
comment on column sys_leaf_alloc.biz_tag is '业务key';
-- 为 max_id 字段添加备注
comment on column sys_leaf_alloc.max_id is '当前已经分配了的最大id';
-- 为 step 字段添加备注
comment on column sys_leaf_alloc.step is '初始步长，也是动态调整的最小步长';
-- 为 description 字段添加备注
comment on column sys_leaf_alloc.description is '业务key的描述';
-- 为 update_time 字段添加备注
comment on column sys_leaf_alloc.update_time is '更新时间';

-- 定义主键约束
alter table sys_leaf_alloc
    add constraint pk_sys_leaf_alloc primary key (biz_tag);