package pl.szmaus.firebirdraks3000.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.utility.MailDetails;
import pl.szmaus.utility.MailsUtility;
import java.util.List;

@Service
public class GetCompanyImp implements GetCompany {
    private final CompanyRepository companyRepository;
    private final MailConfiguration mailConfiguration;

    public GetCompanyImp(CompanyRepository companyRepository, MailConfiguration mailConfiguration) {
        this.companyRepository = companyRepository;
        this.mailConfiguration = mailConfiguration;
    }

    public String returnCompanyEmails(Company company){
        if(company.getFirmFaxNumber()==null){
            return company.getFirmEmailAddress();
        } else {
            return  company.getFirmEmailAddress()+";"+company.getFirmFaxNumber();
        }
    }


    public String verificationTaxId(String taxId) {
         if (taxId != null) {
            taxId = taxId.replaceAll("\\D", "");
         }
         return taxId;
    }

    @Transactional
    public List<Company> findListCompanyFindByTaxId(String invoiceTaxId){
        String correctTaxId=verificationTaxId(invoiceTaxId);
        if (invoiceTaxId!=null ) {
            return companyRepository.findAllByTaxId(correctTaxId);
        } else {
            return null;
        }
    }

    public Boolean ifEmailAddressExists( Company company) {
        return company.getFirmEmailAddress() != null;
    }

    public MailDetails checkEmailAndTaxId( String taxNumber, String companyName) {
        taxNumber=verificationTaxId(taxNumber);
        List<Company> companyList = findListCompanyFindByTaxId(taxNumber);
        MailDetails mailDetails = new MailDetails();
        String toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailClient() : mailConfiguration.getToEmail();
        String bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailClient() : mailConfiguration.getBccEmail();
        if (taxNumber == null ) {
            mailDetails = MailsUtility.createMailDetails(
                    "Dla firmy " + " " + companyName + " nie jest wprowadzony NIP na fakturze",
                    "W Firmie " + " " + companyName + " nie jest wprowadzony NIP na fakturze <br> <strong>Serdecznie pozdrawiamy<br>" + "<strong>Zesp???? NazwaSp????ki<br></p>",
                    bccEmail, toEmail);
        } else if (companyList == null || companyList.size() == 0 ) {
            mailDetails = MailsUtility.createMailDetails(
                    "Firma"+ " " + companyName + " "+ "o NIP-ie " + taxNumber + " nie jest wprowadzona w module administracyjnym w Raks",
                    "Cze????,  <br>Prosz?? sprawd?? czy firma " + companyName  + " jest wprowadzona w module administracyjnym w Raks, poniewa?? nie znaleziono tej firmy " + " o NIP-ie " + taxNumber + " w module administracyjnym w RAKS. <br><br> <strong>Serdecznie pozdrawiamy<br>" + "<strong>Zesp???? NazwaSp????ki<br></p>",
                    bccEmail, toEmail);
        } else if (companyList.size() > 1) {
            mailDetails = MailsUtility.createMailDetails(
                    "Jest wi??cej ni?? jedna firma o tym samym nipie",
                    "Cze????, <br> Znalaz??em wi??cej ni?? jedn?? firm?? w module administracyjnym Raks "  + " o NIPie " + taxNumber + ", nazwa firmy: " + companyList.get(0) + companyList.get(1) + " <br> Nale??y si?? zastanowi??, na kt??ry adres mailowy wysy??a?? informacj??. Nale??y sprawdzi?? czy maile s?? kompletne. <br>" + "<strong>Zesp???? NazwaSp????ki<br></p>>",
                    bccEmail, toEmail);
        } else if (companyList.get(0).getFirmEmailAddress() == null) {
            mailDetails = MailsUtility.createMailDetails(
                    "Brak adresu email dla " + companyName  + " w module administracyjnym w Raks ",
                    "<br> Cze????,  <br> Prosz?? uzupe??nij adres e-mail w module administracyjnym Raks dla firmy " + companyName  + ". <br><br>" + "<strong>Zesp???? NazwaSp????ki<br></p>",
                    bccEmail, toEmail);
        }
        return mailDetails;
    }

    public Boolean ifLackOfInformationInCompany(String taxId){
        taxId=verificationTaxId(taxId);
        List<Company> companyList = findListCompanyFindByTaxId(taxId);
        return taxId== null || companyList == null || companyList.size() == 0 || companyList.size() > 1 || !ifEmailAddressExists(companyList.get(0));
    }
}
