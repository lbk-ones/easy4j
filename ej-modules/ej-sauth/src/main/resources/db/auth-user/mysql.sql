-- MySQL 建表脚本
CREATE TABLE sys_security_user
(
    user_id                 BIGINT       NOT NULL PRIMARY KEY,
    username                VARCHAR(50)  NOT NULL,
    birth_date              DATE,
    sex                     INT,
    phone                   VARCHAR(20),
    id_card                 VARCHAR(18),
    address                 VARCHAR(255),
    password                VARCHAR(100) NOT NULL,
    username_cn             VARCHAR(100),
    username_en             VARCHAR(100),
    nick_name               VARCHAR(100),
    dept_code               VARCHAR(50),
    dept_name               VARCHAR(100),
    account_non_expired     TINYINT      NOT NULL COMMENT '0-过期,1-未过期',
    account_non_locked      TINYINT      NOT NULL COMMENT '0-锁定,1-未锁定',
    credentials_non_expired TINYINT      NOT NULL COMMENT '0-过期,1-未过期',
    enabled                 TINYINT      NOT NULL COMMENT '0-禁用,1-可用',
    pwd_salt                VARCHAR(50),
    create_date             DATETIME,
    update_date             DATETIME,
    org_code                VARCHAR(50),
    org_name                VARCHAR(100),
    tenant_id               VARCHAR(50),
    tenant_name             VARCHAR(100),
    create_by               VARCHAR(50),
    create_name             VARCHAR(100),
    update_by               VARCHAR(50),
    update_name             VARCHAR(100),
    KEY idx_sys_security_user_username (username) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统安全用户表';
    