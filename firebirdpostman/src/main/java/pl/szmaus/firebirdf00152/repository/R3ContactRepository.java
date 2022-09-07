package pl.szmaus.firebirdf00152.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;
import pl.szmaus.firebirdf00152.entity.R3Contact;

public interface R3ContactRepository extends CrudRepository<R3Contact,Long> {
    R3Contact findByTaxId(String s);

}
