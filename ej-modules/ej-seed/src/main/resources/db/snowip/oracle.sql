-- 创建表
CREATE TABLE WORK_IP
(
    IP VARCHAR2 (100),
    NUM NUMBER
);

-- 添加表注释
COMMENT ON TABLE WORK_IP IS '分布式主键IP记录';


ALTER TABLE WORK_IP
    ADD CONSTRAINT PK_WORK_IP PRIMARY KEY (IP);