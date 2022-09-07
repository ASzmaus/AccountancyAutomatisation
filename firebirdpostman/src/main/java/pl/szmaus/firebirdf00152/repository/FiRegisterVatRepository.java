package pl.szmaus.firebirdf00152.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00152.entity.FiRegisterVat;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;

public interface FiRegisterVatRepository extends CrudRepository<FiRegisterVat,Long> {

    FiRegisterVat findByAccountDocId(Long accountDocId);

    @Query( value ="SELECT NEXT VALUE FOR FI_REJESTR_VAT_ID_GEN FROM RDB$DATABASE", nativeQuery = true)
    Long getNextValFI_REJESTR_VAT_ID_GEN();
}
