package easy4j.modules.ltl.transactional.component;

import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.utils.ListTs;
import easy4j.modules.ltl.transactional.LocalMessage;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * LtlTransactionService
 *
 * @author bokun.li
 * @date 2025-05
 */
public class LtlTransactionService implements InitializingBean {
    private DBAccess dbAccess;

    @Override
    public void afterPropertiesSet() throws Exception {
        DBAccessFactory.INIT_DB_FILE_PATH.add("db/lt");
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));
    }

    public void insertLocalMessage(LocalMessage localMessage) throws SQLException {
        dbAccess.saveOne(localMessage, LocalMessage.class);
    }

    public void delete(LocalMessage localMessage) throws SQLException {
        dbAccess.deleteByPrimaryKey(localMessage, LocalMessage.class);
    }

    public LocalMessage findById(String id) throws SQLException {
        LocalMessage byId = dbAccess.getObjectByPrimaryKey(id, LocalMessage.class);
        //Optional<LocalMessage> byId = ltlTransactionMapper.findById(id);
        AtomicReference<LocalMessage> res = new AtomicReference<>(null);
        res.set(byId);
        return res.get();
    }

    public List<LocalMessage> findAllFailed() {
        LocalMessage localMessage = new LocalMessage();
        localMessage.setIsFreeze("is null");
        List<LocalMessage> all = ListTs.newArrayList();
        all = dbAccess.getObjectBy(localMessage, LocalMessage.class);

        return all;
    }

    public void freezeAll(List<LocalMessage> list) {
        for (LocalMessage localMessage : list) {
            localMessage.setIsFreeze("1");
        }
        dbAccess.updateListByPrimaryKey(list, LocalMessage.class);

    }
}