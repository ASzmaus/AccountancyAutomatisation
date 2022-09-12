package pl.szmaus.firebirdf00154.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdf00154.entity.TypeOtherFile;
import pl.szmaus.firebirdf00154.repository.TypeOtherFileRepository;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.exception.EntityNotFoundException;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.firebirdf00154.repository.OtherRemainingFileRepository;

import static java.time.LocalDate.now;

@Service
public class UseOtherRemainingFileImp implements UseOtherRemainingFile {
    private static final int CURRENT_RETURN_MONTH = 1;
    private static final int RECEIVED_DOCUMENTS = 201;
    private final CompanyRepository companyRepository;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final TypeOtherFileRepository typeOtherFileRepository;

    public UseOtherRemainingFileImp(CompanyRepository companyRepository, OtherRemainingFileRepository otherRemainingFileRepository, TypeOtherFileRepository typeOtherFileRepository) {
        this.companyRepository = companyRepository;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.typeOtherFileRepository = typeOtherFileRepository;
    }
    @Transactional
    public OtherRemainingFile findOtherRemainingFileByTaxIdAndName(String returnName,String taxId){
        return otherRemainingFileRepository.findByTaxIdAndName(returnName,taxId);
    }

    @Transactional
    public Boolean checkIfReceivedDocumentFromFirebird(int idCompany) {
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
    public Boolean checkOtherRemainingFile(String returnName, R3Return r3Return){
        OtherRemainingFile otherRemainingFile = findOtherRemainingFileByTaxIdAndName(returnName, r3Return.getNip());
        if(otherRemainingFile==null) {
            createAdditionalRecordForOtherRemainingFile(r3Return,returnName);
            return false;
        }  else{
            return  otherRemainingFile.getName().length() >= 7
                    && otherRemainingFile.getName().substring(0, 7).equals(now().minusMonths(CURRENT_RETURN_MONTH).toString().substring(0, 7));
        }
    }


    @Transactional
    public void createAdditionalRecordForOtherRemainingFile(R3Return r3Return, String nameReturn) {
        OtherRemainingFile otherRemainingFile = new OtherRemainingFile();
        TypeOtherFile typeOtherFile=typeOtherFileRepository.findByName(nameReturn);
        otherRemainingFile.setId(otherRemainingFileRepository.getNextValFI_KART_INNE_POZ_ID_GEN());
        otherRemainingFile.setNumber(r3Return.getNip());
        otherRemainingFile.setCurrency("PLN");
        otherRemainingFile.setName("");
        otherRemainingFile.setDescription(r3Return.getNameOwner());
       if(typeOtherFile!=null)
        otherRemainingFile.setIdTypeOtherFile(typeOtherFile.getId());
        otherRemainingFileRepository.save(otherRemainingFile);
    }
}