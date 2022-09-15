package pl.szmaus.firebirdraks3000.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.szmaus.firebirdraks3000.entity.Company;

import java.util.List;

@Repository
public interface CompanyRepository extends CrudRepository<Company,Integer>{
	List<Company> findAllByTaxId(String taxId);
	List<Company> findAll();
}
