package pl.szmaus.mssql.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.mssql.entity.EAccountantStatus;

@Repository
public interface RaiseStatusRepository extends CrudRepository<EAccountantStatus,Integer> {
}
