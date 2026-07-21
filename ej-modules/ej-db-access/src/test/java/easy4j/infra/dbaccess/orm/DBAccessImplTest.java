package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.domain.OperationLogs;
import easy4j.infra.dbaccess.domain.PageRes;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.orm.conditions.UpdateBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import easy4j.infra.common.utils.EasyMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class DBAccessImplTest {

    IDBAccess idbAccess;
    DynamicDDL dynamicDDL = null;

    @BeforeEach
    void setUp() {
        AccessConfig accessConfig = new AccessConfig();
        DataSource dataSource = getH2DataSource();
        accessConfig.setDataSource(dataSource);
        idbAccess = new DBAccessImpl(accessConfig);
        String s = null;
        dynamicDDL = new DynamicDDL(dataSource, null, OperationLogs.class);
        dynamicDDL.autoDDLByJavaClass(true);

    }

    @AfterEach
    void after() {
        if (dynamicDDL != null) {
            dynamicDDL.close();
        }
    }

    public DataSource getMysql8DataSource() {
        String jdbcUrl = "jdbc:mysql://219.100.186.4:3306/user_test";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        return new TempDataSource(driverClassNameByUrl, jdbcUrl, "root", "xx");
    }

    public DataSource getH2DataSource() {
        String jdbcUrl = "jdbc:h2:mem:testdb";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        return new TempDataSource(driverClassNameByUrl, jdbcUrl, "sa", "");
    }

    @Test
    void save() {
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 100; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("test"+i);
            operationLogs.setBusinessNo("wtqtq"+i);
            operationLogs.setOperatorId((long)i);
            operationLogs.setOperatorName("user"+i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        List<OperationLogs> save = idbAccess.save(objects, OperationLogs.class);
        assertNotNull(save);
        assertEquals(100, save.size());
        assertTrue(save.get(0).getId() > 0);
        System.out.println("save batch test passed");
    }

    @Test
    void testSaveSingleRecord() {
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("testSingle");
        operationLogs.setBusinessNo("single001");
        operationLogs.setOperatorId(999L);
        operationLogs.setOperatorName("singleUser");
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("testSingle", saved.getModule());
        assertEquals("single001", saved.getBusinessNo());
    }

    @Test
    void deleteByCondition() {
        // First insert test data
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 5; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("deleteTest");
            operationLogs.setBusinessNo("del"+i);
            operationLogs.setOperatorId(100L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        idbAccess.save(objects, OperationLogs.class);

        // Delete by condition
        WhereBuild whereBuild = WhereBuild.get().eq("module", "deleteTest");
        int deleted = idbAccess.delete(whereBuild, OperationLogs.class);
        assertEquals(5, deleted);
    }

    @Test
    void deleteById() {
        // Insert one record
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("deleteByIdTest");
        operationLogs.setBusinessNo("delById001");
        operationLogs.setOperatorId(200L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);
        Long id = saved.getId();

        // Delete by id
        OperationLogs deleteParam = new OperationLogs();
        deleteParam.setId(id);
        int deleted = idbAccess.deleteById(deleteParam, OperationLogs.class);
        assertEquals(1, deleted);

        // Verify deletion
        WhereBuild whereBuild = WhereBuild.get().eq("id", id);
        long count = idbAccess.count(whereBuild, OperationLogs.class);
        assertEquals(0, count);
    }

    @Test
    void deleteByIds() {
        // Insert multiple records
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        List<Long> insertedIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("deleteByIdsTest");
            operationLogs.setBusinessNo("delByIds"+i);
            operationLogs.setOperatorId(300L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        List<OperationLogs> saved = idbAccess.save(objects, OperationLogs.class);
        saved.forEach(s -> insertedIds.add(s.getId()));
        List<OperationLogs> list = insertedIds.stream().map(e -> {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setId(e);
            return operationLogs;
        }).toList();
        // Delete by ids
        int deleted = idbAccess.deleteByIds(list, OperationLogs.class);
        assertEquals(3, deleted);
    }

    @Test
    void updateByCondition() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("updateConditionTest");
        operationLogs.setBusinessNo("upd001");
        operationLogs.setOperatorId(400L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Update by condition
        OperationLogs updateParam = new OperationLogs();
        updateParam.setOperatorName("updatedName");
        updateParam.setSuccess(0);

        WhereBuild whereBuild = WhereBuild.get().eq("id", saved.getId());
        int updated = idbAccess.update(updateParam, true, whereBuild, OperationLogs.class);
        assertEquals(1, updated);

        // Verify update
        OperationLogs result = idbAccess.queryOne(whereBuild, OperationLogs.class);
        assertEquals("updatedName", result.getOperatorName());
        assertEquals(0, result.getSuccess());
    }

    @Test
    void updateByUpdateBuild() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("updateBuildTest");
        operationLogs.setBusinessNo("upd002");
        operationLogs.setOperatorId(500L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Update using UpdateBuild
        UpdateBuild updateBuild = UpdateBuild.get()
                .setSql(true, "\"operator_name\" = ?", "updateBuildName")
                .setSql(true, "\"success\" = ?", 0);
        updateBuild.eq("id", saved.getId());

        int updated = idbAccess.update(updateBuild, OperationLogs.class);
        assertEquals(1, updated);
    }

    @Test
    void updateById() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("updateByIdTest");
        operationLogs.setBusinessNo("upd003");
        operationLogs.setOperatorId(600L);
        operationLogs.setOperatorName("originalName");
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Update by id
        OperationLogs updateParam = new OperationLogs();
        updateParam.setId(saved.getId());
        updateParam.setOperatorName("updatedById");
        updateParam.setSuccess(0);

        int updated = idbAccess.updateById(updateParam, true, OperationLogs.class);
        assertEquals(1, updated);

        // Verify update
        WhereBuild whereBuild = WhereBuild.get().eq("id", saved.getId());
        OperationLogs result = idbAccess.queryOne(whereBuild, OperationLogs.class);
        assertEquals("updatedById", result.getOperatorName());
    }

    @Test
    void updateByIds() {
        // Insert multiple records
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("updateByIdsTest");
            operationLogs.setBusinessNo("upd00"+i);
            operationLogs.setOperatorId(700L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        List<OperationLogs> saved = idbAccess.save(objects, OperationLogs.class);
        saved.forEach(s -> ids.add(s.getId()));

        // Update by ids
        ArrayList<OperationLogs> updateParams = ListTs.newArrayList();
        for (Long id : ids) {
            OperationLogs updateParam = new OperationLogs();
            updateParam.setId(id);
            updateParam.setOperatorName("batchUpdated");
            updateParam.setSuccess(0);
            updateParams.add(updateParam);
        }

        int updated = idbAccess.updateByIds(updateParams, true, OperationLogs.class);
        assertEquals(2, updated);
    }

    @Test
    void queryBySql() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("sqlQueryTest");
        operationLogs.setBusinessNo("sql001");
        operationLogs.setOperatorId(800L);
        operationLogs.setOperatorName("sqlUser");
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        idbAccess.save(operationLogs, OperationLogs.class);

        // Query by sql
        String sql = "SELECT * FROM \"sys_operation_logs\" WHERE \"module\" = ? AND \"business_no\" = ?";
        List<OperationLogs> results = idbAccess.query(sql, OperationLogs.class, "sqlQueryTest", "sql001");

        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("sqlQueryTest", results.get(0).getModule());
    }

    @Test
    void queryOneBySql() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("sqlQueryOneTest");
        operationLogs.setBusinessNo("sqlOne001");
        operationLogs.setOperatorId(900L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Query one by sql
        String sql = "SELECT * FROM \"sys_operation_logs\" WHERE \"id\" = ?";
        OperationLogs result = idbAccess.queryOne(sql, OperationLogs.class, saved.getId());

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals("sqlQueryOneTest", result.getModule());
    }

    @Test
    void queryMapBySql() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("mapQueryTest");
        operationLogs.setBusinessNo("map001");
        operationLogs.setOperatorId(1000L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        idbAccess.save(operationLogs, OperationLogs.class);

        // Query as map
        String sql = "SELECT * FROM \"sys_operation_logs\" WHERE \"module\" = ?";
        EasyMap<String, Object> result = idbAccess.queryMapListBySql(sql, "mapQueryTest");

        assertNotNull(result);
    }

    @Test
    void queryByWhereBuild() {
        // Insert test data
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 3; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("whereQueryTest");
            operationLogs.setBusinessNo("where"+i);
            operationLogs.setOperatorId(1100L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        idbAccess.save(objects, OperationLogs.class);

        // Query by WhereBuild
        WhereBuild whereBuild = WhereBuild.get()
                .eq("module", "whereQueryTest")
                .gte("operatorId", 1100L);

        List<OperationLogs> results = idbAccess.query(whereBuild, OperationLogs.class);
        assertNotNull(results);
        assertTrue(results.size() >= 3);
    }

    @Test
    void queryOneByWhereBuild() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("whereQueryOneTest");
        operationLogs.setBusinessNo("whereOne001");
        operationLogs.setOperatorId(1200L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Query one by WhereBuild
        WhereBuild whereBuild = WhereBuild.get().eq("business_no", "whereOne001");
        OperationLogs result = idbAccess.queryOne(whereBuild, OperationLogs.class);

        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void testCount() {
        // Insert test data
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 5; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("countTest");
            operationLogs.setBusinessNo("count"+i);
            operationLogs.setOperatorId(1300L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        idbAccess.save(objects, OperationLogs.class);

        // Count
        WhereBuild whereBuild = WhereBuild.get().eq("module", "countTest");
        long count = idbAccess.count(whereBuild, OperationLogs.class);

        assertEquals(5, count);
    }

    @Test
    void testExists() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("existsTest");
        operationLogs.setBusinessNo("exists001");
        operationLogs.setOperatorId(1400L);
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Check exists
        WhereBuild whereBuild = WhereBuild.get().eq("business_no", "exists001");
        boolean exists = idbAccess.exists(whereBuild, OperationLogs.class);
        assertTrue(exists);

        // Check non-exists
        WhereBuild whereNotExists = WhereBuild.get().eq("business_no", "notExist");
        boolean notExists = idbAccess.exists(whereNotExists, OperationLogs.class);
        assertFalse(notExists);
    }

    @Test
    void queryOneMapByWhereBuild() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("mapQueryOneTest");
        operationLogs.setBusinessNo("mapOne001");
        operationLogs.setOperatorId(1500L);
        operationLogs.setOperatorName("mapUser");
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        idbAccess.save(operationLogs, OperationLogs.class);

        // Query one as map
        WhereBuild whereBuild = WhereBuild.get().eq("business_no", "mapOne001");
        EasyMap<String, Object> result = idbAccess.queryOneMap(whereBuild, OperationLogs.class, true);

        assertNotNull(result);
        assertTrue(result.containsKey("module") || result.containsKey("moduleName"));
    }

    @Test
    void queryPageByWhereBuild() {
        // Insert test data
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 15; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("pageQueryTest");
            operationLogs.setBusinessNo("page"+i);
            operationLogs.setOperatorId(1600L + i);
            operationLogs.setSuccess(1);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        idbAccess.save(objects, OperationLogs.class);

        // Query page
        WhereBuild whereBuild = WhereBuild.get().eq("module", "pageQueryTest").asc("operatorId");
        Page<OperationLogs> page = new Page<>(5);
        page.setPageNo(1);

        PageRes pageRes = idbAccess.queryPage(whereBuild, page, OperationLogs.class);

        assertNotNull(pageRes);
        assertEquals(5,pageRes.getRecords(OperationLogs.class).size());
    }

    @Test
    void testUpdateSkipNull() {
        // Insert test data
        OperationLogs operationLogs = new OperationLogs();
        operationLogs.setModule("skipNullTest");
        operationLogs.setBusinessNo("skipNull001");
        operationLogs.setOperatorId(1700L);
        operationLogs.setOperatorName("originalName");
        operationLogs.setSuccess(1);
        operationLogs.setCreatedAt(new Date());

        OperationLogs saved = idbAccess.save(operationLogs, OperationLogs.class);

        // Update with skip null = true
        OperationLogs updateParam = new OperationLogs();
        updateParam.setId(saved.getId());
        updateParam.setOperatorName("newName");
        // operatorIp is null, should be skipped

        WhereBuild whereBuild = WhereBuild.get().eq("id", saved.getId());
        int updated = idbAccess.update(updateParam, true, whereBuild, OperationLogs.class);
        assertEquals(1, updated);

        // Verify
        OperationLogs result = idbAccess.queryOne(whereBuild, OperationLogs.class);
        assertEquals("newName", result.getOperatorName());
        // originalIp should be null (not updated)
        assertNull(result.getOperatorIp());
    }

    @Test
    void testComplexCondition() {
        // Insert test data
        ArrayList<OperationLogs> objects = ListTs.newArrayList();
        for (int i = 0; i < 5; i++) {
            OperationLogs operationLogs = new OperationLogs();
            operationLogs.setModule("complexTest");
            operationLogs.setBusinessNo("complex"+i);
            operationLogs.setOperatorId((long)i);
            operationLogs.setSuccess(i % 2 == 0 ? 1 : 0);
            operationLogs.setCreatedAt(new Date());
            objects.add(operationLogs);
        }
        idbAccess.save(objects, OperationLogs.class);

        // Complex condition: (operatorId >= 2 AND success = 1) OR module = 'other'
        WhereBuild whereBuild = WhereBuild.get()
                .and(wb -> wb.eq("module", "complexTest").gte("operatorId", 2L).eq("success", 1));

        List<OperationLogs> results = idbAccess.query(whereBuild, OperationLogs.class);
        assertNotNull(results);
        assertTrue(results.size() > 0);
    }
}