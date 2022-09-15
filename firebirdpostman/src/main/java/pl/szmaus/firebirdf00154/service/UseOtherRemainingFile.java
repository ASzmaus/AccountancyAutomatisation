package pl.szmaus.firebirdf00154.service;

import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdraks3000.entity.R3Return;

public interface UseOtherRemainingFile {

    public Boolean checkIfReceivedDocumentFromFirebird(int idCompany);
    public OtherRemainingFile findOtherRemainingFileByTaxIdAndName(String returnName,String taxId);
    public Boolean checkOtherRemainingFile(String returnName, R3Return r3Return);
    public void createAdditionalRecordForOtherRemainingFile(R3Return r3Return, String nameReturn);
    public Boolean ifReceivedDocument(OtherRemainingFile otherRemainingFile, Integer idCompany);
}
