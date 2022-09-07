package pl.szmaus.firebirdf00152.service;

import org.springframework.stereotype.Service;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;
import pl.szmaus.firebirdf00152.entity.R3Contact;
import pl.szmaus.firebirdf00152.repository.R3AccountDocumentRepository;
import pl.szmaus.firebirdf00152.repository.R3ContactRepository;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;

@Service
public class R3AccountDocumentService {
    private static final Long DOCUMENT_KIND_ID = 4L;
    private static final Long ACCOUNT_CODE_ID =265L;
    private static final String  ACCOUNT_CODE = "FS";
    private static final String ACCOUNT_DOCUMENT_STATE = "N";
    private static final Long  DOCUMENT_GROUP_ID = 225L;
    private static final String USER_NAME = "HELPER";
    private final R3AccountDocumentRepository r3AccountDocumentRepository;
    private final R3ContactRepository r3ContactRepository;

    public R3AccountDocumentService(R3AccountDocumentRepository r3AccountDocumentRepository, R3ContactRepository r3ContactRepository) {
        this.r3AccountDocumentRepository = r3AccountDocumentRepository;
        this.r3ContactRepository = r3ContactRepository;
    }

    public R3AccountDocument createR3AccountDocument( String[] arrayRecord) {
        R3AccountDocument r3AccountDocument = r3AccountDocumentRepository.findByDocumentNumber(arrayRecord[0]);
        if( r3AccountDocument==null) {
            R3Contact r3Contact = r3ContactRepository.findByTaxId(arrayRecord[13].replaceAll("\\D", ""));
            r3AccountDocument = new R3AccountDocument();
            r3AccountDocument.setId(r3AccountDocumentRepository.getNextValR3_ACCOUNT_DOCUMENTS_ID_GEN());
            r3AccountDocument.setDocKindId(DOCUMENT_KIND_ID);
            r3AccountDocument.setDocumentNumber(arrayRecord[0]);
            r3AccountDocument.setDocumentDate(LocalDate.parse(arrayRecord[1]));
            r3AccountDocument.setAccountCodeId(ACCOUNT_CODE_ID);
            r3AccountDocument.setAccountCode(ACCOUNT_CODE);
            r3AccountDocument.setAccountDate(LocalDate.parse(arrayRecord[1]));
            r3AccountDocument.setDocumentOpDate(LocalDate.parse(arrayRecord[1]));
            r3AccountDocument.setAccountDocState(ACCOUNT_DOCUMENT_STATE);
            r3AccountDocument.setAccountDocClosed((short) 0);
            r3AccountDocument.setContactId(r3Contact.getId());
            r3AccountDocument.setContactName(r3Contact.getShortName());
            r3AccountDocument.setContactAddress(r3Contact.getStreet());
            r3AccountDocument.setContactNip(r3Contact.getTaxId());
            r3AccountDocument.setContactPlace(r3Contact.getPlace());
            r3AccountDocument.setContactFullName(r3Contact.getFullName());
            r3AccountDocument.setContactBuildingNumber(r3Contact.getBuildingNumber());
            r3AccountDocument.setContactApartmentNumber(r3Contact.getApartmentNumber());
            r3AccountDocument.setContactZipCode(r3Contact.getZipCode());
            r3AccountDocument.setContactEuCode(r3Contact.getEuCode());
            r3AccountDocument.setContactBuildingNumber(r3Contact.getBuildingNumber());
            r3AccountDocument.setZapisKr(true);
            r3AccountDocument.setZapisRs(true);
            r3AccountDocument.setIfImported(true);
            r3AccountDocument.setCIdentUser(USER_NAME);
            r3AccountDocument.setCDate(now());
            r3AccountDocument.setDocGroupId(DOCUMENT_GROUP_ID);
            r3AccountDocumentRepository.save(r3AccountDocument);
            return r3AccountDocument;
        }
        return r3AccountDocument;
    }
}
