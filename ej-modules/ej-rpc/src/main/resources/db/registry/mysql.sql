-- 注册表 sys_e4j_jdbc_reg_data
CREATE TABLE sys_e4j_jdbc_reg_data
(
    id               BIGINT AUTO_INCREMENT COMMENT '主键 自增',
    data_key         VARCHAR(2000) COMMENT '键值key',
    data_value       LONGTEXT COMMENT '键值value',
    data_type        VARCHAR(1) COMMENT '数据类型 临时节点 0，存储节点 1',
    create_date      DATETIME COMMENT '创建时间',
    last_update_date DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='注册表';

-- 给data_key创建索引
CREATE UNIQUE INDEX idx_sys_e4j_jdbc_reg_data_data_key ON sys_e4j_jdbc_reg_data (data_key);

-- 给data_type创建索引
CREATE INDEX idx_sys_e4j_jdbc_reg_data_data_type ON sys_e4j_jdbc_reg_data (data_type);