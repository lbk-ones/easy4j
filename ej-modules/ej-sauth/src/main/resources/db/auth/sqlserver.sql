CREATE TABLE sys_security_session
(
    session_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    username            VARCHAR(128) NOT NULL,
    user_id             BIGINT,
    username_cn         VARCHAR(128),
    username_en         VARCHAR(128),
    nick_name           VARCHAR(128),
    sha_token           VARCHAR(255) NOT NULL,
    sha_token_type      TINYINT,
    sha_token_salt      VARCHAR(128),
    real_token          VARCHAR(512) NOT NULL,
    ip                  VARCHAR(64),
    user_agent          VARCHAR(1024),
    device_id           VARCHAR(128),
    is_invalid          TINYINT  DEFAULT 1,
    expire_time_seconds BIGINT,
    tenant_id           BIGINT       NOT NULL,
    create_by           VARCHAR(128),
    create_name         VARCHAR(128),
    create_time         DATETIME DEFAULT GETDATE(),
    update_by           VARCHAR(128),
    update_name         VARCHAR(128),
    last_update_time    DATETIME DEFAULT GETDATE()
);

EXEC sp_addextendedproperty
    @name = N'MS_Description', @value = '会话表',
    @level0type = N'SCHEMA', @level0name = dbo,
    @level1type = N'TABLE',  @level1name = sys_security_session;

CREATE INDEX idx_username ON sys_security_session (username);
CREATE INDEX idx_user_id ON sys_security_session (user_id);
CREATE INDEX idx_create_time ON sys_security_session (create_time);
CREATE INDEX idx_last_update_time ON sys_security_session (last_update_time);