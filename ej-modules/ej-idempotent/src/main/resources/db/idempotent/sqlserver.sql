CREATE TABLE sys_key_idempotent
(
    ide_key VARCHAR(128) NOT NULL,
    expire_date DATETIME NOT NULL,
    CONSTRAINT sys_key_idempotent_pk PRIMARY KEY (ide_key)
    );

-- 添加字段备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'业务key',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_key_idempotent',
    @level2type = N'COLUMN', @level2name = 'ide_key';

EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'过期时间',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'sys_key_idempotent',
    @level2type = N'COLUMN', @level2name = 'expire_date';