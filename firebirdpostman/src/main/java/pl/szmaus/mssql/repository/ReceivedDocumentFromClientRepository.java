package pl.szmaus.mssql.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClientStatus;

import java.util.List;

@Repository
public interface ReceivedDocumentFromClientRepository extends CrudRepository<ReceivedDocumentFromClient,Integer> {
    ReceivedDocumentFromClient findByIdCompany(Integer idCompany);
    ReceivedDocumentFromClientStatus findByIdReceivedDocumentFromClientStatus(Integer IdReceivedDocumentFromClientStatus);
    List<ReceivedDocumentFromClient> findAllByIdCompany(Integer idCompany);
}
