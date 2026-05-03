-- ================= 用户信息 =================
CREATE TABLE sys_security_user
(
    user_id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                VARCHAR(128) NOT NULL COMMENT '用户名（账号）',
    birth_date              DATE COMMENT '生日',
    sex                     TINYINT  DEFAULT 0 COMMENT '性别 0未知 1男 2女 9未说明/其他',
    phone                   VARCHAR(64) COMMENT '电话号码',
    id_card                 VARCHAR(32) COMMENT '身份证号码',
    email                   VARCHAR(128) COMMENT '邮箱',
    password                VARCHAR(255) COMMENT '密码',
    avatar                  LONGTEXT COMMENT '头像',
    address                 VARCHAR(1024) COMMENT '地址',
    username_cn             VARCHAR(128) COMMENT '姓名（中文）',
    username_en             VARCHAR(128) COMMENT '姓名（英文）',
    nick_name               VARCHAR(128) COMMENT '昵称',
    pwd_error_count         int comment '密码错误次数',
    lock_expire_time        DATETIME comment '密码错误之后锁定到什么时候',
    account_non_expired     TINYINT  DEFAULT 1 COMMENT '是否未过期 0过期 1未过期',
    account_non_locked      TINYINT  DEFAULT 1 COMMENT '是否未锁定 0锁定 1未锁定',
    credentials_non_expired TINYINT  DEFAULT 1 COMMENT '是否密钥未过期 0过期 1未过期',
    last_login_time         DATETIME COMMENT '上一次登录时间',
    pwd_salt                VARCHAR(128) COMMENT '密码随机盐',
    map_code                VARCHAR(128) COMMENT '第三方映射码',
    sp_status               VARCHAR(64) COMMENT '审批状态',
    status                  TINYINT COMMENT '用户状态',
    create_by               VARCHAR(128) COMMENT '创建人',
    create_name             VARCHAR(128) COMMENT '创建人姓名',
    create_time             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by               VARCHAR(128) COMMENT '更新人',
    update_name             VARCHAR(128) COMMENT '更新人姓名',
    last_update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled              TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted              TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    UNIQUE INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息表';

-- ================= 对接多聚道账号 =================
CREATE TABLE sys_security_user_multi
(
    id            bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       bigint       NOT NULL COMMENT '关联 sys_security_user 表的唯一ID',
    identity_type varchar(32)  NOT NULL COMMENT '登录类型：email/phone/did/ldap/oauth2/idCard/password/wechat/qq/github/weibo',
    identifier    varchar(128) NOT NULL COMMENT '唯一标识（账号/邮箱/身份证号/手机号/unionid/openid/第三方uid）',
    credential    varchar(255) DEFAULT NULL COMMENT '密码凭证（密码加密串/第三方token）',
    status        tinyint      DEFAULT 1 COMMENT '状态 1正常 0禁用',
    create_time   datetime     DEFAULT CURRENT_TIMESTAMP,
    update_time   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_identifier (identity_type, identifier),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户多聚道账号登录表';

-- ================= 角色信息 =================
CREATE TABLE sys_security_user_role
(
    role_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code        VARCHAR(500) NOT NULL,
    role_name        VARCHAR(128) COMMENT '角色名称',
    dept_id          BIGINT       NOT NULL COMMENT '部门ID',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    UNIQUE INDEX idx_role_code (role_code),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色信息表';

-- ================= 访问权限明细 =================
CREATE TABLE sys_security_authority
(
    authority_id        VARCHAR(64) PRIMARY KEY COMMENT '权限ID',
    authority_name      VARCHAR(255) NOT NULL COMMENT '权限名称',
    authority_value     LONGTEXT COMMENT '权限值（超大字符串）',
    role_id             BIGINT       NOT NULL COMMENT '角色ID',
    parent_authority_id VARCHAR(64) COMMENT '上级权限ID',
    tree_is_leaf        TINYINT COMMENT '是否树叶子节点',
    tree_is_directory   TINYINT COMMENT '是否树的目录',
    request_uri         VARCHAR(1024) COMMENT '请求地址',
    sort_no             INT COMMENT '排序序号',
    icon                VARCHAR(200) COMMENT '图标',
    locale              VARCHAR(200) COMMENT '前端i18n码',
    authority_type      TINYINT COMMENT '权限类别 1菜单 2资源 3接口',
    ext_map             LONGTEXT COMMENT '额外信息 存json键值对',
    remark              VARCHAR(1024) COMMENT '备注',
    create_by           VARCHAR(128) COMMENT '创建人',
    create_name         VARCHAR(128) COMMENT '创建人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by           VARCHAR(128) COMMENT '更新人',
    update_name         VARCHAR(128) COMMENT '更新人姓名',
    last_update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled          TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted          TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_parent_authority_id (parent_authority_id),
    INDEX idx_authority_type (authority_type),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='访问权限明细表（菜单、资源、接口）';

-- ================= 租户内授权表 =================
CREATE TABLE sys_security_access_authorization
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id         BIGINT  NOT NULL COMMENT '授权主体id',
    auth_subject_type  TINYINT NOT NULL COMMENT '授权主体类型 1用户ID 2部门 3租户 4应用 5角色',
    auth_resource_type TINYINT NOT NULL COMMENT '授权资源类型 1应用 2租户 3部门 4角色 5菜单 6资源 7接口 8权限点 9权限组 10人员组',
    auth_re_id         BIGINT  NOT NULL COMMENT '授权一级资源ID',
    auth_two_re_id     BIGINT COMMENT '授权二级资源ID',
    auth_three_re_id   BIGINT COMMENT '授权三级资源ID',
    with_tenant_id     BIGINT COMMENT '授权资源所属租户ID',
    expire_time        DATETIME COMMENT '过期时间',
    remark             VARCHAR(1024) COMMENT '备注',
    create_by          VARCHAR(128) COMMENT '创建人',
    create_name        VARCHAR(128) COMMENT '创建人姓名',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by          VARCHAR(128) COMMENT '更新人',
    update_name        VARCHAR(128) COMMENT '更新人姓名',
    last_update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled         TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted         TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_with_tenant_subject (with_tenant_id, auth_subject_type, subject_id),
    INDEX idx_subject (auth_subject_type, subject_id),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='租户内授权表（授权资源授权给授权主体）';

-- ================= 部门信息 =================
CREATE TABLE sys_security_dept
(
    dept_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_name        VARCHAR(255) COMMENT '部门名称',
    dept_type        TINYINT COMMENT '部门类别 1科室 2部门 3分公司',
    parent_dept_id   BIGINT COMMENT '上级部门ID',
    leader_id        BIGINT COMMENT '负责人userId',
    leader_phone     VARCHAR(32) COMMENT '负责人联系电话',
    sort_order       INT COMMENT '排序号',
    status           TINYINT COMMENT '状态',
    tenant_id        BIGINT NOT NULL COMMENT '租户ID',
    app_id           BIGINT NOT NULL COMMENT '应用ID',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_parent_dept_id (parent_dept_id),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='部门信息表';

-- ================= 机构信息 =================
CREATE TABLE sys_security_org
(
    org_id           VARCHAR(64) PRIMARY KEY,
    org_name         VARCHAR(255) COMMENT '机构名称',
    org_type         TINYINT COMMENT '机构类型',
    parent_org_id    VARCHAR(64) COMMENT '上级机构ID',
    legal_person     VARCHAR(128) COMMENT '法人代表',
    credit_code      VARCHAR(64) COMMENT '统一社会信用代码',
    email            VARCHAR(128) COMMENT '邮箱',
    address          VARCHAR(1024) COMMENT '地址',
    sort_order       INT COMMENT '排序号',
    status           TINYINT COMMENT '状态',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='机构信息表';

-- ================= 租户信息 =================
CREATE TABLE sys_security_tenant
(
    tenant_id        BIGINT PRIMARY KEY COMMENT '租户编码',
    tenant_name      VARCHAR(255) COMMENT '租户名称',
    app_id           BIGINT NOT NULL COMMENT '应用ID',
    org_id           VARCHAR(64) COMMENT '关联机构ID',
    contact_name     VARCHAR(128) COMMENT '联系人',
    contact_phone    VARCHAR(32) COMMENT '联系电话',
    expire_time      DATETIME COMMENT '过期时间',
    account_limit    INT COMMENT '账号数量限制',
    status           TINYINT COMMENT '状态',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='租户信息表';

-- ================= 会话表 =================
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

-- ================= Token刷新表 =================
CREATE TABLE sys_security_token_refresh
(
    refresh_token_id    VARCHAR(64) PRIMARY KEY,
    username            VARCHAR(128) COMMENT '用户名（账号）',
    user_id             BIGINT COMMENT '用户ID',
    refresh_token       VARCHAR(512) NOT NULL COMMENT '刷新token',
    expire_time_seconds BIGINT COMMENT '过期时间',
    user_agent          VARCHAR(1024) COMMENT 'UA',
    device_id           VARCHAR(128) COMMENT '设备唯一ID',
    ip                  VARCHAR(64) COMMENT '登录IP',
    tenant_id           BIGINT       NOT NULL COMMENT '租户ID',
    login_datetime      DATETIME COMMENT '登录时间',
    log_out_datetime    DATETIME COMMENT '退出登录时间',
    is_invalid          TINYINT  DEFAULT 1 COMMENT '是否失效 1在线 0被踢',
    create_by           VARCHAR(128) COMMENT '创建人',
    create_name         VARCHAR(128) COMMENT '创建人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by           VARCHAR(128) COMMENT '更新人',
    update_name         VARCHAR(128) COMMENT '更新人姓名',
    last_update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled          TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted          TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_username (username),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Token刷新表';

-- ================= App信息 =================
CREATE TABLE sys_security_app
(
    app_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_icon         VARCHAR(1024) COMMENT '应用图标',
    app_name         VARCHAR(255) COMMENT '应用名称',
    app_nick_name    VARCHAR(255) COMMENT '应用别名',
    app_scene        VARCHAR(32) COMMENT '应用场景(内部，外部)',
    app_version      VARCHAR(64) COMMENT '应用版本',
    app_env          VARCHAR(64) COMMENT '应用环境',
    app_type         TINYINT COMMENT '应用分类',
    app_url          VARCHAR(1024) COMMENT '应用跳转地址',
    app_owner        VARCHAR(128) COMMENT '应用负责人',
    app_owner_phone  VARCHAR(32) COMMENT '应用负责人电话',
    app_owner_email  VARCHAR(128) COMMENT '应用负责人邮箱',
    remark           VARCHAR(1024) COMMENT '应用备注',
    status           TINYINT COMMENT '应用状态',
    sp_status        VARCHAR(64) COMMENT '应用审批状态',
    app_is_publish   TINYINT COMMENT '应用是否发布',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='App信息表';

-- ================= 权限日志 =================
CREATE TABLE sys_security_authentication_log
(
    log_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_content      LONGTEXT COMMENT '日志内容',
    log_type         TINYINT NOT NULL COMMENT '日志分类 1登录成功、2退出登录、3密码变更、4邮箱变更、5手机号变更、6强制下线、7加入黑名单、8移除黑名单、9用户已被锁定、10用户后台解锁、11加入白名单、12移除白名单',
    trace_id         VARCHAR(128) COMMENT '链路ID',
    remark           VARCHAR(1024) COMMENT '日志备注',
    consuming        BIGINT COMMENT '耗时(ms)',
    tenant_id        BIGINT  NOT NULL COMMENT '租户ID',
    ip               VARCHAR(20) COMMENT 'ip地址',
    ua               VARCHAR(300) COMMENT 'ua信息',
    device_id        VARCHAR(100) COMMENT '设备唯一ID',
    create_by        VARCHAR(128) COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        VARCHAR(128) COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_log_type_log_name_path (log_type),
    INDEX idx_trace_id (trace_id),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='权限日志表';

-- ================== 黑名单 ==================
CREATE TABLE sys_security_blacklist
(
    id          bigint       NOT NULL AUTO_INCREMENT,
    black_type  tinyint      NOT NULL COMMENT '黑名单类型 1-IP 2-设备指纹 3-设备唯一ID',
    black_value varchar(128) NOT NULL COMMENT '黑名单值 IP/设备ID',
    reason      varchar(255) DEFAULT NULL COMMENT '拉黑原因',
    status      tinyint      DEFAULT 1 COMMENT '1-生效 0-失效',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP,
    expire_time datetime     DEFAULT NULL COMMENT '过期时间（永久则为空）',
    PRIMARY KEY (id),
    UNIQUE KEY idx_type_value (black_type, black_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ================== 白名单 ==================
CREATE TABLE sys_security_whitelist
(
    id          bigint       NOT NULL AUTO_INCREMENT,
    black_type  tinyint      NOT NULL COMMENT '白名单类型 1-IP 2-设备指纹 3-设备唯一ID',
    black_value varchar(128) NOT NULL COMMENT '白名单值 IP/设备ID',
    status      tinyint      DEFAULT 1 COMMENT '1-生效 0-失效',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP,
    expire_time datetime     DEFAULT NULL COMMENT '过期时间（永久则为空）',
    PRIMARY KEY (id),
    UNIQUE KEY idx_type_value (black_type, black_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- ================== 系统配置 ==================
-- 密码输入错误次数
-- 密码输入错误锁定分钟数
-- 是否开启复杂密码检测
-- 是否允许多账号登录
-- 文件存入方式
-- 是否保存登录日志
-- 是否开启黑名单
CREATE TABLE sys_security_settings
(
    id               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    setting_key      varchar(128) NOT NULL COMMENT '配置唯一KEY（英文）,以组名开头',
    setting_name     varchar(64)  NOT NULL COMMENT '配置名称（中文）',
    setting_value    longtext COMMENT '配置值',
    setting_group    varchar(64)  DEFAULT 'default' COMMENT '配置分组：system/login/register/qq/wechat/github',
    setting_type     varchar(32)  DEFAULT 'string' COMMENT '配置类型：string/int/bool/json',
    setting_options  json COMMENT '配置配置列表[{"label":"label1","value":"value1"}]',
    remark           varchar(255) DEFAULT NULL COMMENT '备注说明',
    sort             int          DEFAULT 0 COMMENT '排序',
    create_time      datetime     DEFAULT CURRENT_TIMESTAMP,
    last_update_time datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setting_key (setting_key) COMMENT '配置KEY唯一',
    INDEX idx_group (setting_group),
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统全局配置表';


INSERT INTO sys_security_settings (setting_key, setting_name, setting_value, setting_group, setting_type, setting_options, remark, sort)
VALUES
-- 登录安全组
('login.pwd_error_max_times', '密码输入错误次数', '-1', 'login', 'int', NULL, '密码连续错误达到此次数将锁定账号-1代表不检测', 1),
('login.pwd_error_lock_minutes', '密码输入错误锁定分钟数', '30', 'login', 'int', NULL, '账号锁定时长', 2),
('login.enable_complex_pwd', '是否开启复杂密码检测', 'true', 'login', 'bool', NULL, '开启后密码必须包含字母+数字+特殊字符', 3),
('login.allow_multi_account_login', '多渠道账号登录', 'true', 'login', 'bool', NULL, '是否允许多渠道账号登录', 4),
('login.rate_limit.count', '登录限流次数', '30', 'login', 'int',NULL, '每分钟允许最大登录尝试次数', 5),
('login.rate_limit.minutes', '登录限流时间(分钟)', '1', 'login', 'int',NULL, '登录限流统计时间窗口', 6),

-- 系统配置组
('system.file_storage_type', '文件存入方式', 'local', 'system', 'string', '[
  {"label":"本地存储","value":"local"},
  {"label":"阿里云OSS","value":"aliyun"},
  {"label":"腾讯云COS","value":"tencent"},
  {"label":"MinIO","value":"minio"},
  {"label":"S3","value":"s3"}
]', '系统文件上传存储方式', 10),
('system.save_login_log', '是否保存登录日志', 'true', 'system', 'bool', NULL, '登录成功/失败均记录日志', 11),
('system.enable_blacklist', '是否开启黑名单', 'true', 'system', 'bool', NULL, '开启IP/设备黑名单拦截', 12),
('system.enable_whitelist', '是否开启白名单', 'false', 'system', 'bool', NULL, '开启IP/设备白名单拦截', 13);
