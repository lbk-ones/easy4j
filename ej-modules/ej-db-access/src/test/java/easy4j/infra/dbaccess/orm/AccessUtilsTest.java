package easy4j.infra.dbaccess.orm;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.db.meta.JdbcType;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dialect.DialectFactory;
import easy4j.infra.dbaccess.dialect.impl.MysqlDialect;
import easy4j.infra.dbaccess.dialect.impl.SQLServerDialect;
import easy4j.infra.dbaccess.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import easy4j.infra.dbaccess.orm.vendor.Vendor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccessUtilsTest {
    private AccessConfig config;
    private AccessUtils utils;
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        ContextHolder.remove();
        config = new AccessConfig();
        config.setIgnoreEscape(true);
        connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getDatabaseProductName()).thenReturn("MySQL");
        utils = spy(new AccessUtils(config));
        doReturn(connection).when(utils).getConnection();
    }

    @AfterEach
    void tearDown() {
        ContextHolder.remove();
    }

    @Test
    void batchInsertPreservesOrderNullFilteringAndColumnMetadata() {
        BatchRow first = new BatchRow(null, "first");
        BatchRow second = new BatchRow(7L, "second");
        // 一个只能遍历一次的 Iterable，同时覆盖 param 与 params 合并和 null 过滤。
        AtomicBoolean iterated = new AtomicBoolean();
        Iterable<BatchRow> params = () -> {
            assertFalse(iterated.getAndSet(true));
            return Arrays.asList(null, second, null).iterator();
        };
        Access<BatchRow> access = access(OperateType.INSERT)
                .setParam(first).setParams(params);

        RuntimeContext<BatchRow> context = utils.toContext(access);

        assertEquals(List.of(first, second), context.getParams());
        assertEquals(List.of("batch_id", "display_name"), names(context.getColumnInfoList()));
        assertEquals(List.of("display_name", "batch_id", "display_name"), names(context.getInsertFields()));
        assertEquals(List.of(0, 1, 1), groups(context.getInsertFields()));
        assertEquals(List.of(0, 1), groups(context.getIdList()));
        assertNull(Wd.value(context.getIdList().get(0).getColumnValue()));
        assertEquals(7L, Wd.value(context.getIdList().get(1).getColumnValue()));
        assertEquals(1, context.getAutoIncrementList().size());
        assertSame(context.getColumnInfoList().get(0), context.getAutoIncrementList().get(0));
        for (AccessField field : context.getInsertFields()) {
            if ("display_name".equals(field.getColumnName())) {
                assertEquals("upper(?)", field.getPlaceHolder());
                assertEquals("label", field.getAlias());
                assertEquals(JdbcType.VARCHAR, ((Wd<?>) field.getColumnValue()).getJdbcType());
            }
        }
    }

    @Test
    void batchUpdatePreservesSkipNullAndGroups() {
        Access<BatchRow> access = access(OperateType.UPDATE).setSkipNullIs(true)
                .setParams(List.of(new BatchRow(1L, null), new BatchRow(2L, "second")));

        RuntimeContext<BatchRow> context = utils.toContext(access);

        assertEquals(List.of("display_name"), names(context.getUpdateFields()));
        assertEquals(List.of(1), groups(context.getUpdateFields()));
        assertEquals("second", Wd.value(context.getUpdateFields().get(0).getColumnValue()));
        assertEquals(List.of(0, 1), groups(context.getIdList()));
        assertTrue(context.getInsertFields().isEmpty());

        context = utils.toContext(access.setSkipNullIs(false));
        assertEquals(List.of(0, 1), groups(context.getUpdateFields()));
        assertNull(context.getUpdateFields().get(0).getColumnValue());
    }

    @Test
    void noParamsPreservesPrimaryKeysAndColumnMetadata() {
        RuntimeContext<BatchRow> context = utils.toContext(access(OperateType.SELECT)
                .setParams(Arrays.asList(null, null)).setPrimaryKeys(List.of(3L, 4L)));

        assertTrue(context.getParams().isEmpty());
        assertEquals(List.of("batch_id", "display_name"), names(context.getColumnInfoList()));
        assertEquals(List.of(3L, 4L), context.getIdList().stream()
                .map(field -> Wd.value(field.getColumnValue())).toList());
        assertEquals(List.of(0, 0), groups(context.getIdList()));
        assertTrue(context.getInsertFields().isEmpty());
        assertTrue(context.getUpdateFields().isEmpty());
    }

    @Test
    void cachedMetadataStillUsesCurrentConfiguration() {
        Access<BatchRow> access = access(OperateType.INSERT)
                .setParams(List.of(new BatchRow(1L, "first"), new BatchRow(2L, "second")));
        assertEquals(List.of("batch_id", "display_name"), names(utils.toContext(access).getColumnInfoList()));
        config.setFieldNameToUnderline(false);
        assertEquals(List.of("batchId", "displayName"), names(utils.toContext(access).getColumnInfoList()));
    }

    @Test
    void allIgnoredFieldsStillPreserveParams() {
        IgnoredRow first = new IgnoredRow();
        IgnoredRow second = new IgnoredRow();
        RuntimeContext<IgnoredRow> context = utils.toContext(new Access<IgnoredRow>()
                .setClazz(IgnoredRow.class).setOperateType(OperateType.INSERT).setParams(List.of(first, second)));
        assertEquals(List.of(first, second), context.getParams());
        assertTrue(context.getColumnInfoList().isEmpty());
        assertTrue(context.getInsertFields().isEmpty());
    }

    @Test
    void fieldMetadataIsSharedAcrossCallsAndAccessUtilsInstances() throws Exception {
        Access<CachedRow> access = new Access<CachedRow>().setClazz(CachedRow.class)
                .setOperateType(OperateType.INSERT).setParam(new CachedRow("first"));
        try (MockedStatic<Vendor> vendor = mockStatic(Vendor.class, CALLS_REAL_METHODS);
             MockedStatic<AccessUtils> staticUtils = mockStatic(AccessUtils.class, CALLS_REAL_METHODS)) {
            RuntimeContext<CachedRow> first = utils.toContext(access);
            // 修改返回上下文不能污染缓存，也不能串入下一次调用。
            first.getColumnInfoList().get(0).setColumnName("modified");
            AccessUtils another = spy(new AccessUtils(config));
            doReturn(connection).when(another).getConnection();
            RuntimeContext<CachedRow> second = another.toContext(access.setParam(new CachedRow("second")));
            RuntimeContext<CachedRow> empty = another.toContext(access.setParam(null));
            assertEquals(List.of("cached_value"), names(second.getColumnInfoList()));
            assertEquals("second", Wd.value(second.getInsertFields().get(0).getColumnValue()));
            assertEquals(List.of("cached_value"), names(empty.getColumnInfoList()));
            assertNull(empty.getColumnInfoList().get(0).getColumnValue());
            assertNotSame(first.getColumnInfoList().get(0), second.getColumnInfoList().get(0));
            assertNotSame(first.getInsertFields().get(0), second.getInsertFields().get(0));
            java.lang.reflect.Field field = ReflectUtil.getField(CachedRow.class, "cachedValue");
            vendor.verify(() -> Vendor.skipColumn(field), times(1));
            vendor.verify(() -> Vendor.isPk(field), times(1));
            vendor.verify(() -> Vendor.isAutoIncrement(field), times(1));
            vendor.verify(() -> Vendor.getColumnName(field), times(1));
            staticUtils.verify(() -> AccessUtils.resolveWdField(field), times(1));
        }
    }

    @Test
    void dynamicMapMetadataIsReadOnEveryCall() throws Exception {
        Dialect dialect = mock(Dialect.class);
        when(dialect.getDbType()).thenReturn("mysql");
        DatabaseColumnMetadata firstColumn = new DatabaseColumnMetadata();
        firstColumn.setColumnName("first_column");
        DatabaseColumnMetadata secondColumn = new DatabaseColumnMetadata();
        secondColumn.setColumnName("second_column");
        when(dialect.getColumns(null, null, "dynamic_table"))
                .thenReturn(List.of(firstColumn), List.of(secondColumn));
        when(dialect.getPrimaryKes(null, null, "dynamic_table")).thenReturn(List.of());
        Access<Object> access = new Access<>().setReturnMap(true).setTableName("dynamic_table")
                .setOperateType(OperateType.INSERT)
                .setMapParams(List.of(EasyMap.of("first_column", "first")));
        try (MockedStatic<DialectFactory> factory = mockStatic(DialectFactory.class)) {
            factory.when(() -> DialectFactory.get(connection)).thenReturn(dialect);
            RuntimeContext<Object> first = utils.toContext(access);
            RuntimeContext<Object> second = utils.toContext(access
                    .setMapParams(List.of(EasyMap.of("second_column", "second"))));
            assertEquals(List.of("first_column"), names(first.getColumnInfoList()));
            assertEquals(List.of("second_column"), names(second.getColumnInfoList()));
            assertEquals("first", Wd.value(first.getInsertFields().get(0).getColumnValue()));
            assertEquals("second", Wd.value(second.getInsertFields().get(0).getColumnValue()));
            verify(dialect, times(2)).getColumns(null, null, "dynamic_table");
            verify(dialect, times(2)).getPrimaryKes(null, null, "dynamic_table");
        }
    }

    @Test
    void patchTemplatesAreReusedWithoutSharingMutableFields() {
        Access<BatchRow> access = access(OperateType.SELECT);
        RuntimeContext<BatchRow> first = utils.toContext(access);
        AccessField id = first.getColumnInfoList().get(0);
        id.setColumnName("modified");
        id.setEscapeColumnName("modified_escape");
        id.setColumnValue(99L);
        id.setGroup(7);
        id.setPkIs(false);
        id.setAutoIncrementIs(false);
        id.setSkipPsSet(true);
        AccessField name = first.getColumnInfoList().get(1);
        name.setAlias("modified_alias");
        name.setPlaceHolder("modified_placeholder");
        clearInvocations(utils);

        RuntimeContext<BatchRow> second = utils.toContext(access);

        assertEquals(List.of("batch_id", "display_name"), names(second.getColumnInfoList()));
        AccessField secondId = second.getColumnInfoList().get(0);
        assertNotSame(id, secondId);
        assertEquals("batch_id", secondId.getEscapeColumnName());
        assertNull(secondId.getColumnValue());
        assertEquals(0, secondId.getGroup());
        assertTrue(secondId.isPkIs());
        assertTrue(secondId.isAutoIncrementIs());
        assertFalse(secondId.isSkipPsSet());
        assertSame(secondId, second.getAutoIncrementList().get(0));
        assertEquals("label", second.getColumnInfoList().get(1).getAlias());
        assertEquals("upper(?)", second.getColumnInfoList().get(1).getPlaceHolder());
        verify(utils, never()).fn("batchId");
        verify(utils, never()).fn("displayName");
        verify(utils, never()).sqlNameEscape(eq("batch_id"), any(Dialect.class), eq(false));
        verify(utils, never()).sqlNameEscape(eq("display_name"), any(Dialect.class), eq(false));
    }

    @Test
    void templateCacheUsesCurrentDialectAndEscapeConfiguration() {
        Access<ReservedRow> access = new Access<ReservedRow>().setClazz(ReservedRow.class)
                .setOperateType(OperateType.SELECT);
        try (MockedStatic<DialectFactory> factory = mockStatic(DialectFactory.class)) {
            factory.when(() -> DialectFactory.get(connection)).thenReturn(new MysqlDialect(connection));
            assertEquals("order", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
            config.setIgnoreEscape(false);
            assertEquals("`order`", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
            SQLServerDialect sqlServer = spy(new SQLServerDialect(connection));
            doReturn(DbType.SQL_SERVER.getDb()).when(sqlServer).getDbType();
            factory.when(() -> DialectFactory.get(connection)).thenReturn(sqlServer);
            assertEquals("[order]", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
            config.setIgnoreEscape(true);
            assertEquals("order", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
        }
    }

    @Test
    void customDialectTemplatesAreNotCached() {
        Access<ReservedRow> access = new Access<ReservedRow>().setClazz(ReservedRow.class)
                .setOperateType(OperateType.SELECT);
        config.setIgnoreEscape(false);
        Dialect dialect = mock(Dialect.class);
        when(dialect.getDbType()).thenReturn("mysql");
        when(dialect.escape("order")).thenReturn("first_escape", "second_escape");
        try (MockedStatic<DialectFactory> factory = mockStatic(DialectFactory.class)) {
            factory.when(() -> DialectFactory.get(connection)).thenReturn(dialect);
            assertEquals("first_escape", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
            assertEquals("second_escape", utils.toContext(access).getColumnInfoList().get(0).getEscapeColumnName());
        }
    }

    private Access<BatchRow> access(OperateType type) {
        return new Access<BatchRow>().setClazz(BatchRow.class).setOperateType(type);
    }

    private List<String> names(List<AccessField> fields) {
        return fields.stream().map(AccessField::getColumnName).toList();
    }

    private List<Integer> groups(List<AccessField> fields) {
        return fields.stream().map(AccessField::getGroup).toList();
    }

    static class BatchRow {
        @JdbcColumn(name = "batchId", isPrimaryKey = true, autoIncrement = true)
        Long batchId;

        @JdbcIgnore
        String ignored = "ignored";

        @JdbcColumn(name = "displayName", alias = "label", placeHolder = "upper(?)", jdbcType = JdbcType.VARCHAR)
        String displayName;

        BatchRow(Long batchId, String displayName) {
            this.batchId = batchId;
            this.displayName = displayName;
        }
    }

    static class IgnoredRow {
        @JdbcIgnore
        String ignored;
    }

    static class CachedRow {
        @JdbcColumn(name = "cachedValue")
        String cachedValue;

        CachedRow(String value) {
            cachedValue = value;
        }
    }

    static class ReservedRow {
        @JdbcColumn(name = "order")
        String order;
    }
}
