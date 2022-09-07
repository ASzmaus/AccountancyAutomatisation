package pl.szmaus.firebirdf00152.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.szmaus.firebirdf00152.entity.FiRegisterVatOther;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;

import java.time.LocalDate;

public interface R3AccountDocumentRepository extends CrudRepository<R3AccountDocument,Long> {
    R3AccountDocument findByDocumentNumber(String documentNumber);

    @Query( value ="SELECT NEXT VALUE FOR R3_ACCOUNT_DOCUMENTS_ID_GEN FROM RDB$DATABASE", nativeQuery = true)
    Long getNextValR3_ACCOUNT_DOCUMENTS_ID_GEN();

}
