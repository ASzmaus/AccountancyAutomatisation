package pl.szmaus.mssql.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.mssql.entity.EAccountant;

import java.util.List;

@Repository
public interface EAccountantRepository extends CrudRepository<EAccountant,Integer> {
    List<EAccountant> findAll();
}
