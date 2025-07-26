-- DB2 建表脚本
CREATE TABLE sys_security_user
(
    user_id                 BIGINT       NOT NULL PRIMARY KEY,
    username                VARCHAR(50)  NOT NULL,
    birth_date              DATE,
    sex                     INTEGER,
    phone                   VARCHAR(20),
    id_card                 VARCHAR(18),
    address                 VARCHAR(255),
    password                VARCHAR(100) NOT NULL,
    username_cn             VARCHAR(100),
    username_en             VARCHAR(100),
    nick_name               VARCHAR(100),
    dept_code               VARCHAR(50),
    dept_name               VARCHAR(100),
    account_non_expired     SMALLINT     NOT NULL CHECK (account_non_expired IN (0, 1)),
    account_non_locked      SMALLINT     NOT NULL CHECK (account_non_locked IN (0, 1)),
    credentials_non_expired SMALLINT     NOT NULL CHECK (credentials_non_expired IN (0, 1)),
    enabled                 SMALLINT     NOT NULL CHECK (enabled IN (0, 1)),
    pwd_salt                VARCHAR(50),
    create_date             TIMESTAMP,
    update_date             TIMESTAMP,
    org_code                VARCHAR(50),
    org_name                VARCHAR(100),
    tenant_id               VARCHAR(50),
    tenant_name             VARCHAR(100),
    create_by               VARCHAR(50),
    create_name             VARCHAR(100),
    update_by               VARCHAR(50),
    update_name             VARCHAR(100)
);

-- 唯一索引
CREATE UNIQUE INDEX idx_sys_security_user_username ON sys_security_user (username);
    