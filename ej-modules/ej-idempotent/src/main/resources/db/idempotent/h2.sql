CREATE TABLE SYS_KEY_IDEMPOTENT
(
    IDE_KEY   VARCHAR(128) NOT NULL COMMENT '业务key',
    EXPIRE_DATE TIMESTAMP    NOT NULL COMMENT '过期时间',
    PRIMARY KEY (IDE_KEY)
);