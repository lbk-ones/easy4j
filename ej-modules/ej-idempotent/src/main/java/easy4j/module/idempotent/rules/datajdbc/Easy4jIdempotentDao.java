package easy4j.module.idempotent.rules.datajdbc;

import org.springframework.data.repository.CrudRepository;

public interface Easy4jIdempotentDao extends CrudRepository<Easy4jKeyIdempotent,String> {
}
