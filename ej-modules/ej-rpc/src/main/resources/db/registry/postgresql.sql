-- 注册表 sys_e4j_jdbc_reg_data
create table sys_e4j_jdbc_reg_data
(
    id               bigserial primary key,
    data_key         varchar(2000),
    data_value       text,
    data_type        varchar(1),
    create_date      timestamp,
    last_update_date timestamp
);

-- 表注释
comment on table sys_e4j_jdbc_reg_data is '注册表';

-- 字段注释（建表时注释不生效时补充）
comment on column sys_e4j_jdbc_reg_data.id is '主键 自增';
comment on column sys_e4j_jdbc_reg_data.data_key is '键值key';
comment on column sys_e4j_jdbc_reg_data.data_value is '键值value';
comment on column sys_e4j_jdbc_reg_data.data_type is '数据类型 临时节点 0，存储节点 1';
comment on column sys_e4j_jdbc_reg_data.create_date is '创建时间';
comment on column sys_e4j_jdbc_reg_data.last_update_date is '更新时间';

-- 给data_key创建索引
create unique index idx_sys_e4j_jdbc_reg_data_data_key on sys_e4j_jdbc_reg_data (data_key);

-- 给data_type创建索引
create index idx_sys_e4j_jdbc_reg_data_data_type on sys_e4j_jdbc_reg_data (data_type);