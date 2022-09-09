package pl.szmaus.firebirdraks3000.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.firebirdraks3000.entity.R3Return;

import java.util.List;

@Repository
public interface R3ReturnRepository extends CrudRepository<R3Return,Integer> {

    List<R3Return> findAll();
}