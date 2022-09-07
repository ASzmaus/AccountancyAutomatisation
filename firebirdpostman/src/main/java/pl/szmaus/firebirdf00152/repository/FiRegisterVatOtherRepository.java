package pl.szmaus.firebirdf00152.repository;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.szmaus.firebirdf00152.entity.FiRegisterVatOther;

public interface FiRegisterVatOtherRepository extends CrudRepository<FiRegisterVatOther,Long> {

    FiRegisterVatOther findByVatId(Long vatId);

    @Query( value ="SELECT NEXT VALUE FOR FI_REJESTR_VAT_POZ_ID_GEN FROM RDB$DATABASE", nativeQuery = true)
    Long getNextValFI_REJESTR_VAT_POZ_ID_GEN();
}
