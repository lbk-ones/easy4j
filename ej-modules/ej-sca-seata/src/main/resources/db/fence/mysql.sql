-- -------------------------------- THE SCRIPT USE TCC FENCE  --------------------------------
CREATE TABLE IF NOT EXISTS `SYS_TCC_FENCE_LOG`
(
    `XID`          VARCHAR(128) NOT NULL COMMENT 'global id',
    `BRANCH_ID`    BIGINT       NOT NULL COMMENT 'branch id',
    `ACTION_NAME`  VARCHAR(64)  NOT NULL COMMENT 'action name',
    `STATUS`       TINYINT      NOT NULL COMMENT 'status(tried:1;committed:2;rollbacked:3;suspended:4)',
    `GMT_CREATE`   DATETIME(3)  NOT NULL COMMENT 'create time',
    `GMT_MODIFIED` DATETIME(3)  NOT NULL COMMENT 'update time',
    PRIMARY KEY (`XID`, `BRANCH_ID`),
    KEY `IDX_GMT_MODIFIED` (`GMT_MODIFIED`),
    KEY `IDX_STATUS` (`STATUS`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8MB4;