-- ================= 用户信息 =================
# CREATE TABLE sys_security_user
# (
#     user_id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
#     username                VARCHAR(128) NOT NULL COMMENT '用户名（账号）',
#     birth_date              DATE COMMENT '生日',
#     sex                     TINYINT  DEFAULT 0 COMMENT '性别 0未知 1男 2女 9未说明/其他',
#     phone                   VARCHAR(64) COMMENT '电话号码',
#     id_card                 VARCHAR(32) COMMENT '身份证号码',
#     email                   VARCHAR(128) COMMENT '邮箱',
#     password                VARCHAR(255) COMMENT '密码',
#     avatar                  LONGTEXT COMMENT '头像',
#     address                 VARCHAR(1024) COMMENT '地址',
#     username_cn             VARCHAR(128) COMMENT '姓名（中文）',
#     username_en             VARCHAR(128) COMMENT '姓名（英文）',
#     nick_name               VARCHAR(128) COMMENT '昵称',
#     account_non_expired     TINYINT  DEFAULT 1 COMMENT '是否未过期 0过期 1未过期',
#     account_non_locked      TINYINT  DEFAULT 1 COMMENT '是否未锁定 0锁定 1未锁定',
#     credentials_non_expired TINYINT  DEFAULT 1 COMMENT '是否密钥未过期 0过期 1未过期',
#     pwd_salt                VARCHAR(128) COMMENT '密码随机盐',
#     map_code                VARCHAR(128) COMMENT '第三方映射码',
#     sp_status               VARCHAR(64) COMMENT '审批状态',
#     status                  TINYINT COMMENT '用户状态',
#     create_by               BIGINT COMMENT '创建人',
#     create_name             VARCHAR(128) COMMENT '创建人姓名',
#     create_time             DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
#     update_by               BIGINT COMMENT '更新人',
#     update_name             VARCHAR(128) COMMENT '更新人姓名',
#     last_update_time        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
#     is_enabled              TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
#     is_deleted              TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
#     INDEX idx_username (username),
#     INDEX idx_phone (phone),
#     INDEX idx_email (email),
#     INDEX idx_create_time (create_time),
#     INDEX idx_last_update_time (last_update_time)
# ) ENGINE = InnoDB
#   DEFAULT CHARSET = utf8mb4
#   COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息表';

-- ================= 角色信息 =================
CREATE TABLE sys_security_user_role
(
    role_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name        VARCHAR(128) COMMENT '角色名称',
    dept_id          BIGINT NOT NULL COMMENT '部门ID',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
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
    authority_type      TINYINT COMMENT '权限类别 1菜单 2按钮 3接口',
    ext_map             LONGTEXT COMMENT '额外信息 存json键值对',
    remark              VARCHAR(1024) COMMENT '备注',
    create_by           BIGINT COMMENT '创建人',
    create_name         VARCHAR(128) COMMENT '创建人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by           BIGINT COMMENT '更新人',
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
  COLLATE = utf8mb4_unicode_ci COMMENT ='访问权限明细表（菜单、按钮、接口）';

-- ================= 租户内授权表 =================
CREATE TABLE sys_security_access_authorization
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id         VARCHAR(128) NOT NULL COMMENT '授权主体id',
    auth_subject_type  TINYINT      NOT NULL COMMENT '授权主体类型 1用户ID 2部门 3租户 4应用 5角色',
    auth_resource_type TINYINT      NOT NULL COMMENT '授权资源类型 1应用 2租户 3部门 4角色 5菜单 6按钮 7接口 8权限点 9权限组 10人员组',
    auth_re_id         BIGINT       NOT NULL COMMENT '授权一级资源ID',
    auth_two_re_id     BIGINT COMMENT '授权二级资源ID',
    auth_three_re_id   BIGINT COMMENT '授权三级资源ID',
    with_tenant_id     BIGINT       NOT NULL COMMENT '授权资源所属租户ID',
    expire_time        DATETIME COMMENT '过期时间',
    remark             VARCHAR(1024) COMMENT '备注',
    create_by          BIGINT COMMENT '创建人',
    create_name        VARCHAR(128) COMMENT '创建人姓名',
    create_time        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by          BIGINT COMMENT '更新人',
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
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
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
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
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
    tenant_id        VARCHAR(64) PRIMARY KEY COMMENT '租户编码',
    tenant_name      VARCHAR(255) COMMENT '租户名称',
    app_id           BIGINT NOT NULL COMMENT '应用ID',
    org_id           VARCHAR(64) COMMENT '关联机构ID',
    contact_name     VARCHAR(128) COMMENT '联系人',
    contact_phone    VARCHAR(32) COMMENT '联系电话',
    expire_time      DATETIME COMMENT '过期时间',
    account_limit    INT COMMENT '账号数量限制',
    status           TINYINT COMMENT '状态',
    remark           VARCHAR(1024) COMMENT '备注',
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
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
# CREATE TABLE sys_security_session
# (
#     session_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
#     username            VARCHAR(128) NOT NULL COMMENT '用户名（账号）',
#     user_id             BIGINT COMMENT '用户ID',
#     username_cn         VARCHAR(128) COMMENT '用户姓名（中文）',
#     username_en         VARCHAR(128) COMMENT '用户姓名（英文）',
#     nick_name           VARCHAR(128) COMMENT '昵称',
#     sha_token           VARCHAR(255) NOT NULL COMMENT '访问token',
#     sha_token_type      TINYINT COMMENT '访问token生成方式',
#     sha_token_salt      VARCHAR(128) COMMENT '访问token随机值',
#     real_token          VARCHAR(512) NOT NULL COMMENT '原始TOKEN(通过原始token生成访问token)',
#     ip                  VARCHAR(64) COMMENT '会话IP',
#     user_agent          VARCHAR(1024) COMMENT 'UA',
#     device_id           VARCHAR(128) COMMENT '设备唯一ID',
#     is_invalid          TINYINT  DEFAULT 1 COMMENT '是否失效 1在线 0被踢',
#     expire_time_seconds BIGINT COMMENT '过期时间',
#     tenant_id           BIGINT       NOT NULL COMMENT '租户ID',
#     create_by           VARCHAR(128) COMMENT '创建人',
#     create_name         VARCHAR(128) COMMENT '创建人姓名',
#     create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
#     update_by           VARCHAR(128) COMMENT '更新人',
#     update_name         VARCHAR(128) COMMENT '更新人姓名',
#     last_update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
#     INDEX idx_username (username),
#     INDEX idx_user_id (user_id),
#     INDEX idx_create_time (create_time),
#     INDEX idx_last_update_time (last_update_time)
# ) ENGINE = InnoDB
#   DEFAULT CHARSET = utf8mb4
#   COLLATE = utf8mb4_unicode_ci COMMENT ='会话表';

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
    create_by           BIGINT COMMENT '创建人',
    create_name         VARCHAR(128) COMMENT '创建人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by           BIGINT COMMENT '更新人',
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
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='App信息表';

-- ================= 权限日志表 =================
CREATE TABLE sys_security_logs
(
    log_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_name         VARCHAR(255) NOT NULL COMMENT '日志标题',
    log_content      LONGTEXT COMMENT '日志内容',
    log_type         TINYINT      NOT NULL COMMENT '日志分类 1登录 2退出登录 3高危操作 4接口日志 5系统日志',
    url_path         VARCHAR(2048) COMMENT '发起日志的链接',
    trace_id         VARCHAR(128) COMMENT '链路ID',
    log_level        TINYINT  DEFAULT 0 COMMENT '日志前端是否可见 0不可见 1可见',
    remark           VARCHAR(1024) COMMENT '日志备注',
    consuming        BIGINT COMMENT '耗时(ms)',
    tenant_id        BIGINT       NOT NULL COMMENT '租户ID',
    create_by        BIGINT COMMENT '创建人',
    create_name      VARCHAR(128) COMMENT '创建人姓名',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by        BIGINT COMMENT '更新人',
    update_name      VARCHAR(128) COMMENT '更新人姓名',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_enabled       TINYINT  DEFAULT 1 COMMENT '是否启用 1启用 0禁用',
    is_deleted       TINYINT  DEFAULT 0 COMMENT '是否删除 1已删除 0未删除',
    INDEX idx_create_time (create_time),
    INDEX idx_last_update_time (last_update_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='权限日志表';
