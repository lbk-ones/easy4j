package easy4j.modules.ltl.transactional;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface LtlTransactionMapper extends CrudRepository<LocalMessage, String> {


    List<LocalMessage> findLocalMessagesByIsFreezeIsNull();

}
