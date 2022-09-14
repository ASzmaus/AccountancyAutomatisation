package pl.szmaus.mssql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdf00154.service.UseOtherRemainingFile;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.repository.ReceivedDocumentFromClientRepository;
import pl.szmaus.utility.DateUtility;
import java.util.List;

import static java.time.LocalDate.now;

@Service
public class ReceiveDocumentFromClientImp implements ReceiveDocumentFromClient {
    private static final int PREVOIUS_MONTH = 1;
    private static final Integer FIRST_INFO_STATUS_ID = 1;
    private static final Integer FIRST_REMINDER_STATUS_ID = 2;
    private static final Integer RECEIVED_DOCUMENT_STATUS_ID = 3;
    private static final Integer SECOND_REMINDER_STATUS_ID = 4;
    private static final Integer NO_EMAIL_ID = 5;
    private static final Integer WRONG_NIP_ID = 6;

    private final ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository;
    private final CompanyRepository companyRepository;
    private final GetCompany getCompany;
    private final UseOtherRemainingFile useOtherRemainingFile;
    private final DateUtility dateUtility;

    public ReceiveDocumentFromClientImp(ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository, CompanyRepository companyRepository, GetCompany getCompany, UseOtherRemainingFile useOtherRemainingFile, DateUtility dateUtility) {
        this.receivedDocumentFromClientRepository = receivedDocumentFromClientRepository;
        this.companyRepository = companyRepository;
        this.getCompany = getCompany;
        this.useOtherRemainingFile = useOtherRemainingFile;
        this.dateUtility = dateUtility;
    }
    @Transactional
    public void saveReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idReceivedDocumentFromClientStatus) {
        Company company = companyRepository.findById(idCompany)
                .orElseThrow(() -> new RuntimeException("No firms for this Id"));
        receivedDocumentFromClient.setIdCompany(idCompany);
        receivedDocumentFromClient.setData(now().minusMonths(PREVOIUS_MONTH).toString().substring(0, 7));
        receivedDocumentFromClient.setIdReceivedDocumentFromClientStatus(idReceivedDocumentFromClientStatus);
        receivedDocumentFromClient.setNumber(company.getRaksNumber());
        receivedDocumentFromClientRepository.save(receivedDocumentFromClient);
    }

    @Transactional
    public void editReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idReceivedDocumentFromClientStatus)  {
        receivedDocumentFromClient.setIdReceivedDocumentFromClientStatus(idReceivedDocumentFromClientStatus);
        receivedDocumentFromClient.setData(now().minusMonths(PREVOIUS_MONTH).toString().substring(0, 7));
        receivedDocumentFromClientRepository.save(receivedDocumentFromClient);
    }

    @Transactional
    public void saveStatusForDocuments(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idStatusDocuments) {
        if (receivedDocumentFromClient == null) {
            ReceivedDocumentFromClient receivedDocumentsFromClients1 = new ReceivedDocumentFromClient();
            saveReceivedDocumentFromClient(receivedDocumentsFromClients1, idCompany, idStatusDocuments);
        } else {
           editReceivedDocumentFromClient(receivedDocumentFromClient, idStatusDocuments);
        }
    }

    public Boolean ifNotReceivedDocumentFirstInfo(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return (receivedDocumentFromClient == null || !receivedDocumentFromClient.getData().equals(dateUtility.extractPreviousMonthAndYear()) )
               && now().isBefore(dateUtility.dateReminder2Documents());
    }
    public Boolean ifNotReceivedDocumentFirstReminder(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return receivedDocumentFromClient.getIdReceivedDocumentFromClientStatus() == FIRST_INFO_STATUS_ID
               && receivedDocumentFromClient.getData().equals(dateUtility.extractPreviousMonthAndYear())
               && now().isAfter(dateUtility.dateReminder2Documents().minusDays(1))
               && now().isBefore(dateUtility.dateReminder3Documents());
    }
    public Boolean ifNotReceivedDocumentSecondReminder(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return receivedDocumentFromClient.getIdReceivedDocumentFromClientStatus() == FIRST_REMINDER_STATUS_ID
               && receivedDocumentFromClient.getData().equals(dateUtility.extractPreviousMonthAndYear())
               && now().isAfter(dateUtility.dateReminder3Documents());
    }

    public void checkStatusForDocuments(ReceivedDocumentFromClient receivedDocumentFromClient, List<Company> companyList, OtherRemainingFile otherRemainingFile) {
        if ( companyList == null || companyList.size() == 0 || companyList.size()>1) {
            editReceivedDocumentFromClient(receivedDocumentFromClient, WRONG_NIP_ID);
        } else if (!getCompany.ifEmailAddressExists(companyList.get(0))) {
            editReceivedDocumentFromClient(receivedDocumentFromClient, NO_EMAIL_ID);
        } else if (useOtherRemainingFile.ifReceivedDocument(otherRemainingFile, companyList.get(0).getId())) {
           saveStatusForDocuments(receivedDocumentFromClient, companyList.get(0).getId(), RECEIVED_DOCUMENT_STATUS_ID);
        } else if (ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient)) {
            saveStatusForDocuments(receivedDocumentFromClient, companyList.get(0).getId(), FIRST_INFO_STATUS_ID);
        } else if (ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)) {
            editReceivedDocumentFromClient(receivedDocumentFromClient, FIRST_REMINDER_STATUS_ID);
        } else if (ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient)) {
            editReceivedDocumentFromClient(receivedDocumentFromClient, SECOND_REMINDER_STATUS_ID);
        }
    }
}
