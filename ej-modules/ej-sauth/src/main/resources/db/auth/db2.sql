CREATE TABLE sys_security_session
(
    session_id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username            VARCHAR(128) NOT NULL,
    user_id             BIGINT,
    username_cn         VARCHAR(128),
    username_en         VARCHAR(128),
    nick_name           VARCHAR(128),
    sha_token           VARCHAR(255) NOT NULL,
    sha_token_type      SMALLINT,
    sha_token_salt      VARCHAR(128),
    real_token          VARCHAR(512) NOT NULL,
    ip                  VARCHAR(64),
    user_agent          VARCHAR(1024),
    device_id           VARCHAR(128),
    is_invalid          SMALLINT  DEFAULT 1,
    expire_time_seconds BIGINT,
    tenant_id           BIGINT       NOT NULL,
    create_by           VARCHAR(128),
    create_name         VARCHAR(128),
    create_time         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by           VARCHAR(128),
    update_name         VARCHAR(128),
    last_update_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  sys_security_session IS '会话表';
COMMENT ON COLUMN sys_security_session.username IS '用户名（账号）';
COMMENT ON COLUMN sys_security_session.user_id IS '用户ID';
COMMENT ON COLUMN sys_security_session.username_cn IS '用户姓名（中文）';
COMMENT ON COLUMN sys_security_session.username_en IS '用户姓名（英文）';
COMMENT ON COLUMN sys_security_session.nick_name IS '昵称';
COMMENT ON COLUMN sys_security_session.sha_token IS '访问token';
COMMENT ON COLUMN sys_security_session.sha_token_type IS '访问token生成方式';
COMMENT ON COLUMN sys_security_session.sha_token_salt IS '访问token随机值';
COMMENT ON COLUMN sys_security_session.real_token IS '原始TOKEN(通过原始token生成访问token)';
COMMENT ON COLUMN sys_security_session.ip IS '会话IP';
COMMENT ON COLUMN sys_security_session.user_agent IS 'UA';
COMMENT ON COLUMN sys_security_session.device_id IS '设备唯一ID';
COMMENT ON COLUMN sys_security_session.is_invalid IS '是否失效 1在线 0被踢';
COMMENT ON COLUMN sys_security_session.expire_time_seconds IS '过期时间';
COMMENT ON COLUMN sys_security_session.tenant_id IS '租户ID';
COMMENT ON COLUMN sys_security_session.create_by IS '创建人';
COMMENT ON COLUMN sys_security_session.create_name IS '创建人姓名';
COMMENT ON COLUMN sys_security_session.create_time IS '创建时间';
COMMENT ON COLUMN sys_security_session.update_by IS '更新人';
COMMENT ON COLUMN sys_security_session.update_name IS '更新人姓名';
COMMENT ON COLUMN sys_security_session.last_update_time IS '更新时间';

CREATE INDEX idx_username ON sys_security_session (username);
CREATE INDEX idx_user_id ON sys_security_session (user_id);
CREATE INDEX idx_create_time ON sys_security_session (create_time);
CREATE INDEX idx_last_update_time ON sys_security_session (last_update_time);