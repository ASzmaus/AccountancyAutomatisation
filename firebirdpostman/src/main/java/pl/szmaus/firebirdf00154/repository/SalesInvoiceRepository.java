package pl.szmaus.firebirdf00154.repository;

import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;

import java.util.List;

public interface SalesInvoiceRepository extends CrudRepository<SalesInvoice,Integer> {

        List<SalesInvoice> findAll();

}
