package pl.szmaus.firebirdf00154.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdf00154.entity.TypeOtherFile;
import pl.szmaus.firebirdf00154.repository.TypeOtherFileRepository;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.exception.EntityNotFoundException;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.firebirdf00154.repository.OtherRemainingFileRepository;
import static java.time.LocalDate.now;

@Service
public class OtherRemainingFileService {
    private static final Integer RECEIVED_DOCUMENTS = 201;
    private final CompanyRepository companyRepository;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final TypeOtherFileRepository typeOtherFileRepository;

    public OtherRemainingFileService(CompanyRepository companyRepository, OtherRemainingFileRepository otherRemainingFileRepository, TypeOtherFileRepository typeOtherFileRepository) {
        this.companyRepository = companyRepository;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.typeOtherFileRepository = typeOtherFileRepository;
    }

    @Transactional
    public Boolean checkIfRecivedDocumentFromFirebird(int idCompany) {
        Company company = companyRepository.findById(idCompany).orElseThrow(EntityNotFoundException::new);
        if (company.getRaksNumber()== null)
            throw new IllegalArgumentException("NumberRaks cannot be null");
        String  numberRaks = company.getRaksNumber().toString();
        OtherRemainingFile otherRemainingFile= otherRemainingFileRepository.findByNumberAndIdTypeOtherFile(numberRaks,RECEIVED_DOCUMENTS);
                return otherRemainingFile!=null
                    && otherRemainingFile.getName().length()>=7
                    && otherRemainingFile.getName().substring(0,7).equals(now().minusMonths(1).toString().substring(0,7));
    }

    @Transactional
    public void createAdditionalRecordForOtherRemainingFile(String number, String name, String currency, String description,String nameReturn) {
        OtherRemainingFile otherRemainingFile = new OtherRemainingFile();
        TypeOtherFile typeOtherFile=typeOtherFileRepository.findByName(nameReturn);
        otherRemainingFile.setId(otherRemainingFileRepository.getNextValFI_KART_INNE_POZ_ID_GEN());
        otherRemainingFile.setNumber(number);
        otherRemainingFile.setCurrency(currency);
        otherRemainingFile.setName(name);
        otherRemainingFile.setDescription(description);
       if(typeOtherFile!=null)
        otherRemainingFile.setIdTypeOtherFile(typeOtherFile.getId());
        otherRemainingFileRepository.save(otherRemainingFile);
    }
}