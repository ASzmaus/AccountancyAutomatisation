package pl.szmaus.firebirdf00154.repository;

import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;

public interface SalesInvoiceRepository extends CrudRepository<SalesInvoice,Integer> {

        SalesInvoice findAllByNumber(String number);
        SalesInvoice findByGuid(String guid);

}
