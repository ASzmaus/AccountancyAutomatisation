package pl.szmaus.firebirdf00154.repository;

import org.springframework.data.repository.CrudRepository;
import pl.szmaus.firebirdf00154.entity.TypeOtherFile;

public interface TypeOtherFileRepository extends CrudRepository<TypeOtherFile,Integer> {

 TypeOtherFile findByName(String  nameReturn);
}
