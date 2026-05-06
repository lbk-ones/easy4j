create table sys_work_ip
(
    ip  varchar(100),
    num int(20),
    primary key (ip)
) comment '分布式主键ip记录';