package pl.szmaus.firebirdf00154attachment.repository;

import org.springframework.data.repository.CrudRepository;

import pl.szmaus.firebirdf00154attachment.entity.R3DocumentFiles;


public interface R3DocumentFilesRepository extends CrudRepository<R3DocumentFiles,String> {
   R3DocumentFiles findByGuid(String guid);

}
