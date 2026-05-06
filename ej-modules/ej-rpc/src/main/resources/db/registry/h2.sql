-- 注册表 sys_e4j_jdbc_reg_data
create table sys_e4j_jdbc_reg_data
(
    id               bigint auto_increment comment '主键 自增',
    data_key         varchar(2000) comment '键值key',
    data_value       longvarchar comment '键值value',
    data_type        varchar(1) comment '数据类型 临时节点 0，存储节点 1',
    create_date      timestamp comment '创建时间',
    last_update_date timestamp comment '更新时间',
    primary key (id)
);

-- 给data_key创建索引
create unique index idx_sys_e4j_jdbc_reg_data_data_key on sys_e4j_jdbc_reg_data (data_key);

-- 给data_type创建索引
create index idx_sys_e4j_jdbc_reg_data_data_type on sys_e4j_jdbc_reg_data (data_type);