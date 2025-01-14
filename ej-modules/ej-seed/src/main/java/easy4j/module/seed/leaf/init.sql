-- MYSQL

CREATE TABLE `LEAF_ALLOC` (
      `BIZ_TAG` VARCHAR(128)  NOT NULL DEFAULT '',
      `MAX_ID` BIGINT(20) NOT NULL DEFAULT '1',
      `STEP` INT(11) NOT NULL,
      `DESCRIPTION` VARCHAR(256)  DEFAULT NULL,
      `UPDATE_TIME` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (`BIZ_TAG`)
) ENGINE=INNODB;


-- ORACLE
create table LEAF_ALLOC
(
    BIZ_TAG     VARCHAR2(128)          not null
        primary key,
    MAX_ID      NUMBER(20) default (1) not null,
    STEP        NUMBER(11)             not null,
    DESCRIPTION VARCHAR2(256),
    UPDATE_TIME DATE                   not null
)
/

comment on column LEAF_ALLOC.BIZ_TAG is '业务key'
/

comment on column LEAF_ALLOC.MAX_ID is '当前已经分配了的最大id'
/

comment on column LEAF_ALLOC.STEP is '初始步长，也是动态调整的最小步长'
/

comment on column LEAF_ALLOC.DESCRIPTION is '业务key的描述'
/

comment on column LEAF_ALLOC.UPDATE_TIME is '更新时间'
/



