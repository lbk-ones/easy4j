CREATE TABLE KEY_IDEMPOTENT
(
    [IDE_KEY] VARCHAR(128) NOT NULL,
    EXPIRE_DATE DATETIME NOT NULL,
    CONSTRAINT KEY_IDEMPOTENT_PK PRIMARY KEY ([IDE_KEY])
);

-- 添加字段备注
EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'业务key',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'KEY_IDEMPOTENT',
    @level2type = N'COLUMN', @level2name = 'IDE_KEY';

EXEC sys.sp_addextendedproperty
    @name = N'MS_Description',
    @value = N'过期时间',
    @level0type = N'SCHEMA', @level0name = 'dbo',
    @level1type = N'TABLE',  @level1name = 'KEY_IDEMPOTENT',
    @level2type = N'COLUMN', @level2name = 'EXPIRE_DATE';