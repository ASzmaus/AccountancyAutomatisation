package pl.szmaus.firebirdraks3000.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.firebirdraks3000.entity.R3DefinitionReturn;
import pl.szmaus.firebirdraks3000.entity.R3Return;

@Repository
public interface R3DefinitionReturnRepository extends CrudRepository<R3DefinitionReturn,Integer> {

}