package pl.szmaus.firebirdf00152.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00152.entity.KRDocumentBook;

public interface KRDocumentBookRepository extends CrudRepository<KRDocumentBook, Long> {

    @Query( value ="SELECT NEXT VALUE FOR KR_DOKUMENTY_KSIEGA_ID_GEN FROM RDB$DATABASE", nativeQuery = true)
    Long getNextValKR_DOKUMENTY_KSIEGA_ID_GEN();

    KRDocumentBook findByAccountDocId(Long accountDodId);
}
