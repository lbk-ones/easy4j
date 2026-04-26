CREATE TABLE sys_security_user
(
    user_id                 NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username                VARCHAR2(128) NOT NULL,
    birth_date              DATE,
    sex                     NUMBER(3) DEFAULT 0,
    phone                   VARCHAR2(64),
    id_card                 VARCHAR2(32),
    email                   VARCHAR2(128),
    password                VARCHAR2(255),
    avatar                  CLOB,
    address                 VARCHAR2(1024),
    username_cn             VARCHAR2(128),
    username_en             VARCHAR2(128),
    nick_name               VARCHAR2(128),
    account_non_expired     NUMBER(3) DEFAULT 1,
    account_non_locked      NUMBER(3) DEFAULT 1,
    credentials_non_expired NUMBER(3) DEFAULT 1,
    pwd_salt                VARCHAR2(128),
    map_code                VARCHAR2(128),
    sp_status               VARCHAR2(64),
    status                  NUMBER(3),
    create_by               VARCHAR2(128),
    create_name             VARCHAR2(128),
    create_time             TIMESTAMP DEFAULT SYSTIMESTAMP,
    update_by               VARCHAR2(128),
    update_name             VARCHAR2(128),
    last_update_time        TIMESTAMP DEFAULT SYSTIMESTAMP,
    is_enabled              NUMBER(3) DEFAULT 1,
    is_deleted              NUMBER(3) DEFAULT 0
);

COMMENT ON TABLE sys_security_user IS '用户信息表';
COMMENT ON COLUMN sys_security_user.username IS '用户名（账号）';
COMMENT ON COLUMN sys_security_user.birth_date IS '生日';
COMMENT ON COLUMN sys_security_user.sex IS '性别 0未知 1男 2女 9未说明/其他';
COMMENT ON COLUMN sys_security_user.phone IS '电话号码';
COMMENT ON COLUMN sys_security_user.id_card IS '身份证号码';
COMMENT ON COLUMN sys_security_user.email IS '邮箱';
COMMENT ON COLUMN sys_security_user.password IS '密码';
COMMENT ON COLUMN sys_security_user.avatar IS '头像';
COMMENT ON COLUMN sys_security_user.address IS '地址';
COMMENT ON COLUMN sys_security_user.username_cn IS '姓名（中文）';
COMMENT ON COLUMN sys_security_user.username_en IS '姓名（英文）';
COMMENT ON COLUMN sys_security_user.nick_name IS '昵称';
COMMENT ON COLUMN sys_security_user.account_non_expired IS '是否未过期 0过期 1未过期';
COMMENT ON COLUMN sys_security_user.account_non_locked IS '是否未锁定 0锁定 1未锁定';
COMMENT ON COLUMN sys_security_user.credentials_non_expired IS '是否密钥未过期 0过期 1未过期';
COMMENT ON COLUMN sys_security_user.pwd_salt IS '密码随机盐';
COMMENT ON COLUMN sys_security_user.map_code IS '第三方映射码';
COMMENT ON COLUMN sys_security_user.sp_status IS '审批状态';
COMMENT ON COLUMN sys_security_user.status IS '用户状态';
COMMENT ON COLUMN sys_security_user.create_by IS '创建人';
COMMENT ON COLUMN sys_security_user.create_name IS '创建人姓名';
COMMENT ON COLUMN sys_security_user.create_time IS '创建时间';
COMMENT ON COLUMN sys_security_user.update_by IS '更新人';
COMMENT ON COLUMN sys_security_user.update_name IS '更新人姓名';
COMMENT ON COLUMN sys_security_user.last_update_time IS '更新时间';
COMMENT ON COLUMN sys_security_user.is_enabled IS '是否启用 1启用 0禁用';
COMMENT ON COLUMN sys_security_user.is_deleted IS '是否删除 1已删除 0未删除';

CREATE INDEX idx_username ON sys_security_user (username);
CREATE INDEX idx_phone ON sys_security_user (phone);
CREATE INDEX idx_email ON sys_security_user (email);
CREATE INDEX idx_create_time ON sys_security_user (create_time);
CREATE INDEX idx_last_update_time ON sys_security_user (last_update_time);