package pl.szmaus.firebirdraks3000.service;

import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.utility.MailDetails;

import java.util.List;

public interface GetCompany {

    public String returnCompanyEmails(Company company);

    public String verificationTaxId(String taxId);

    public List<Company> findListCompanyFindByTaxId(String invoiceTaxId);

    public Boolean ifEmailAddressExists( Company company);

    public MailDetails checkEmailAndTaxId(String taxNumber, String companyName);

    public Boolean ifLackOfInformationInCompany(String taxId);
}
