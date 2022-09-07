package pl.szmaus.mssql.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.mssql.entity.Raise;

import java.util.List;

@Repository
public interface RaiseRepository extends CrudRepository<Raise,Integer> {
    List<Raise> findAll();
}
