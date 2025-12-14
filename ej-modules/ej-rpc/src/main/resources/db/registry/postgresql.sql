-- 注册表 sys_e4j_jdbc_reg_data
CREATE TABLE sys_e4j_jdbc_reg_data
(
    id               BIGSERIAL PRIMARY KEY,
    data_key         VARCHAR(2000),
    data_value       TEXT,
    data_type        VARCHAR(1),
    create_date      TIMESTAMP,
    last_update_date TIMESTAMP
);

-- 表注释
COMMENT ON TABLE sys_e4j_jdbc_reg_data IS '注册表';

-- 字段注释（建表时注释不生效时补充）
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.id IS '主键 自增';
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.data_key IS '键值key';
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.data_value IS '键值value';
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.data_type IS '数据类型 临时节点 0，存储节点 1';
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.create_date IS '创建时间';
COMMENT ON COLUMN sys_e4j_jdbc_reg_data.last_update_date IS '更新时间';

-- 给data_key创建索引
CREATE UNIQUE INDEX idx_sys_e4j_jdbc_reg_data_data_key ON sys_e4j_jdbc_reg_data (data_key);

-- 给data_type创建索引
CREATE INDEX idx_sys_e4j_jdbc_reg_data_data_type ON sys_e4j_jdbc_reg_data (data_type);