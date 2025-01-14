package easy4j.modules.ltl.transactional;

import org.springframework.data.repository.CrudRepository;


public interface LtlTransactionMapper extends CrudRepository<LocalMessage, String> {


}
