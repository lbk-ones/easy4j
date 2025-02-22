package easy4j.modules.ltl.transactional.component;
import easy4j.module.base.utils.ListTs;
import easy4j.modules.ltl.transactional.LocalMessage;
import easy4j.modules.ltl.transactional.LtlTransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LtlTransactionService {

    @Autowired
    LtlTransactionMapper ltlTransactionMapper;

    public void insertOrUpdateLocalMessage(LocalMessage localMessage){
        ltlTransactionMapper.save(localMessage);
    }

    public void delete(LocalMessage localMessage){
        ltlTransactionMapper.delete(localMessage);
    }

    public LocalMessage findById(String id){
        Optional<LocalMessage> byId = ltlTransactionMapper.findById(id);
        AtomicReference<LocalMessage> res = new AtomicReference<>(null);
        byId.ifPresent(res::set);
        return res.get();
    }
    public List<LocalMessage> findAllFailed(){
        Iterable<LocalMessage> all = ltlTransactionMapper.findLocalMessagesByIsFreezeIsNull();
        return ListTs.newArrayList(all.iterator());
    }

    public void freezeAll(List<LocalMessage> list){
        for (LocalMessage localMessage : list) {
            localMessage.setIsFreeze("1");
        }
        ltlTransactionMapper.saveAll(list);
    }
}
