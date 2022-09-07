package pl.szmaus.firebirdf00154.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;

public interface OtherRemainingFileRepository extends CrudRepository<OtherRemainingFile,Integer> {

  OtherRemainingFile findByNumberAndIdTypeOtherFile(String Number, Integer idTypeOtherFile);
  OtherRemainingFile findByIdTypeOtherFile(Integer interestRateId);

  @Query("SELECT otherRemainingFile FROM TypeOtherFile typeOtherFile JOIN OtherRemainingFile otherRemainingFile ON typeOtherFile.id=otherRemainingFile.idTypeOtherFile WHERE typeOtherFile.name =:nametypeOtherFile AND otherRemainingFile.number =:numberTaxId")
  OtherRemainingFile findByTaxIdAndName(@Param("nametypeOtherFile") String name, @Param("numberTaxId") String number);

  @Query( value ="SELECT NEXT VALUE FOR FI_KART_INNE_POZ_ID_GEN FROM RDB$DATABASE", nativeQuery = true)
  Integer getNextValFI_KART_INNE_POZ_ID_GEN();

}
