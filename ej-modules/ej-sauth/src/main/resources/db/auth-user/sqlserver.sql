CREATE TABLE sys_security_user
(
    user_id                 BIGINT IDENTITY (1,1) PRIMARY KEY,
    username                VARCHAR(128) NOT NULL,
    birth_date              DATE,
    sex                     TINYINT  DEFAULT 0,
    phone                   VARCHAR(64),
    id_card                 VARCHAR(32),
    email                   VARCHAR(128),
    password                VARCHAR(255),
    avatar                  VARCHAR(MAX),
    address                 VARCHAR(1024),
    username_cn             VARCHAR(128),
    username_en             VARCHAR(128),
    nick_name               VARCHAR(128),
    account_non_expired     TINYINT  DEFAULT 1,
    account_non_locked      TINYINT  DEFAULT 1,
    credentials_non_expired TINYINT  DEFAULT 1,
    pwd_salt                VARCHAR(128),
    map_code                VARCHAR(128),
    sp_status               VARCHAR(64),
    status                  TINYINT,
    create_by               VARCHAR(128),
    create_name             VARCHAR(128),
    create_time             DATETIME DEFAULT GETDATE(),
    update_by               VARCHAR(128),
    update_name             VARCHAR(128),
    last_update_time        DATETIME DEFAULT GETDATE(),
    is_enabled              TINYINT  DEFAULT 1,
    is_deleted              TINYINT  DEFAULT 0
);

EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = '用户信息表',
     @level0type = N'SCHEMA', @level0name = dbo,
     @level1type = N'TABLE', @level1name = sys_security_user;

CREATE INDEX idx_username ON sys_security_user (username);
CREATE INDEX idx_phone ON sys_security_user (phone);
CREATE INDEX idx_email ON sys_security_user (email);
CREATE INDEX idx_create_time ON sys_security_user (create_time);
CREATE INDEX idx_last_update_time ON sys_security_user (last_update_time);