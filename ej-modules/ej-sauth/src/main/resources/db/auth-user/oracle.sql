-- Oracle 建表脚本
CREATE TABLE sys_security_user
(
    user_id                 NUMBER(19)    NOT NULL PRIMARY KEY,
    username                VARCHAR2(50)  NOT NULL,
    birth_date              DATE,
    sex                     NUMBER(10),
    phone                   VARCHAR2(20),
    id_card                 VARCHAR2(18),
    address                 VARCHAR2(255),
    password                VARCHAR2(100) NOT NULL,
    username_cn             VARCHAR2(100),
    username_en             VARCHAR2(100),
    nick_name               VARCHAR2(100),
    dept_code               VARCHAR2(50),
    dept_name               VARCHAR2(100),
    account_non_expired     NUMBER(1)     NOT NULL CHECK (account_non_expired IN (0, 1)),
    account_non_locked      NUMBER(1)     NOT NULL CHECK (account_non_locked IN (0, 1)),
    credentials_non_expired NUMBER(1)     NOT NULL CHECK (credentials_non_expired IN (0, 1)),
    enabled                 NUMBER(1)     NOT NULL CHECK (enabled IN (0, 1)),
    pwd_salt                VARCHAR2(50),
    create_date             TIMESTAMP(6),
    update_date             TIMESTAMP(6),
    org_code                VARCHAR2(50),
    org_name                VARCHAR2(100),
    tenant_id               VARCHAR2(50),
    tenant_name             VARCHAR2(100),
    create_by               VARCHAR2(50),
    create_name             VARCHAR2(100),
    update_by               VARCHAR2(50),
    update_name             VARCHAR2(100)
);

-- 唯一索引
CREATE UNIQUE INDEX idx_sys_security_user_username ON sys_security_user (username);
    