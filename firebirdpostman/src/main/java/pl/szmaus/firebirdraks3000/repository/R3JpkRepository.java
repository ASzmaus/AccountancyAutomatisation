package pl.szmaus.firebirdraks3000.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.firebirdraks3000.entity.R3Jpk;
import pl.szmaus.firebirdraks3000.entity.R3Return;

@Repository
public interface R3JpkRepository extends CrudRepository<R3Jpk,Integer> {
    R3Jpk findByIdR3Return(Integer  id);
}