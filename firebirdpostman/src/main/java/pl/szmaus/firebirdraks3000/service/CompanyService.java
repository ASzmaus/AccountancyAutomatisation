package pl.szmaus.firebirdraks3000.service;

import org.springframework.stereotype.Service;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.utility.MailDetails;
import pl.szmaus.utility.MailsUtility;
import java.util.List;
import java.util.Map;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void verificationIfTaxIdIsValid() {
        Iterable<Company> allFirms = companyRepository.findAll();
        allFirms.forEach(d -> {
            if (d.getTaxId() != null) {
               d.setTaxId(d.getTaxId().replaceAll("\\D", ""));
            }
        });
    }

    public List<Company> findListCompanyFindByTaxId(String invoiceTaxId){
        if (invoiceTaxId!=null ) {
            return companyRepository.findAllByTaxId(invoiceTaxId.replaceAll("\\D", ""));
        } else{
            return null;
        }
    }
    public Boolean ifEmailAddressExists( Company company) {
        return company.getFirmEmailAddress() != null;
    }

    public MailDetails checkEmailAndTaxId(List<Company> companyList, byte[] attachmentInvoice, String taxNumber, String companyName,Map<String,byte[]> imagesMap, String toEmail, String bccEmail) {
        MailDetails mailDetails = new MailDetails();
        if (companyList.size() ==0 || companyList.get(0) == null) {
            mailDetails = MailsUtility.createMailDetails(
                    "Firma"+ " " + companyName + " "+ "o NIP-ie " + taxNumber + " nie jest wprowadzona w module administracyjnym w Raks",
                    "Cześć,  <br>Proszę sprawdź czy firma " + companyName  + " jest wprowadzona w module administracyjnym w Raks, ponieważ nie znaleziono tej firmy " + " o NIP-ie " + taxNumber + " w module administracyjnym w RAKS. <br><br> <strong>Serdecznie pozdrawiamy<br>" + "<strong>Zespół NazwaSpółki<br></p>",
                    bccEmail, toEmail, attachmentInvoice, imagesMap);
        } else if (companyList.size() > 1) {
            mailDetails = MailsUtility.createMailDetails(
                    "Jest więcej niż jedna firma o tym samym nipie",
                    "Cześć, <br> Znalazłem więcej niż jedną firmę w module administracyjnym Raks "  + " o NIPie " + taxNumber + ", nazwa firmy: " + companyList.get(0) + companyList.get(1) + " <br> Należy się zastanowić, na który adres mailowy wysyłać informację. Należy sprawdzić czy maile są kompletne. <br>" + "<strong>Zespół NazwaSpółki<br></p>>",
                    bccEmail, toEmail, attachmentInvoice, imagesMap);
        } else if (companyList.get(0).getFirmEmailAddress() == null) {
            mailDetails = MailsUtility.createMailDetails(
                    "Brak adresu email dla " + companyName  + " w module administracyjnym w Raks ",
                    "<br> Cześć,  <br> Proszę uzupełnij adres e-mail w module administracyjnym Raks dla firmy " + companyName  + ". <br><br>" + "<strong>Zespół NazwaSpółki<br></p>",
                    bccEmail, toEmail, attachmentInvoice, imagesMap);
        }
        return mailDetails;
    }

    public Boolean ifLackOfInformationInCompany(String taxId){
        List<Company> companyList = findListCompanyFindByTaxId(taxId);
        return companyList.size() == 0 || companyList.get(0) == null || companyList.size() > 1 || !ifEmailAddressExists(companyList.get(0));
    }
}
