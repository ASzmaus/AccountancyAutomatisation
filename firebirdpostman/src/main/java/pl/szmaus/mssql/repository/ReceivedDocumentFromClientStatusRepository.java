package pl.szmaus.mssql.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
//import pl.firebirdraks3000firebirdf00154.entity.Companys;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClientStatus;

@Repository
public interface ReceivedDocumentFromClientStatusRepository extends CrudRepository<ReceivedDocumentFromClientStatus,Integer> {
}
