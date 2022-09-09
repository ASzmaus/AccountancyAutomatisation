package pl.szmaus.firebirdf00154.service;

import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;

public interface OtherRemainingFileService {

    public Boolean checkIfReceivedDocumentFromFirebird(int idCompany);
    public void createAdditionalRecordForOtherRemainingFile(String number, String name, String currency, String description,String nameReturn);
    public OtherRemainingFile findOtherRemainingFileByTaxIdAndName(String returnName,String taxId);
    public void checkOtherRemainingFile(String returnName, String taxId, String name, String currency, String nameClient);
}
