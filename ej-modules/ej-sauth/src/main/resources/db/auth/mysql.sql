CREATE TABLE sys_security_session
(
    session_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(128) NOT NULL COMMENT '用户名（账号）',
    user_id             BIGINT COMMENT '用户ID',
    username_cn         VARCHAR(128) COMMENT '用户姓名（中文）',
    username_en         VARCHAR(128) COMMENT '用户姓名（英文）',
    nick_name           VARCHAR(128) COMMENT '昵称',
    sha_token           VARCHAR(255) NOT NULL COMMENT '访问token',
    sha_token_type      TINYINT COMMENT '访问token生成方式',
    sha_token_salt      VARCHAR(128) COMMENT '访问token随机值',
    real_token          VARCHAR(512) NOT NULL COMMENT '原始TOKEN(通过原始token生成访问token)',
    ip                  VARCHAR(64) COMMENT '会话IP',
    user_agent          VARCHAR(1024) COMMENT 'UA',
    device_id           VARCHAR(128) COMMENT '设备唯一ID',
    is_invalid          TINYINT  DEFAULT 1 COMMENT '是否失效 1在线 0被踢',
    expire_time_seconds BIGINT COMMENT '过期时间',
    tenant_id           BIGINT       NOT NULL COMMENT '租户ID',
    create_by           VARCHAR(128) COMMENT '创建人',
    create_name         VARCHAR(128) COMMENT '创建人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by           VARCHAR(128) COMMENT '更新人',
    update_name         VARCHAR(128) COMMENT '更新人姓名',
    last_update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='会话表';