package pl.szmaus.mssql.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.repository.CompanyRepository;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.repository.ReceivedDocumentFromClientRepository;
import static java.time.LocalDate.now;

@Service
public class ReceivedDocumentFromClientServiceImp  implements ReceivedDocumentFromClientService{

    private final ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository;
    private final CompanyRepository companyRepository;

    public ReceivedDocumentFromClientServiceImp(ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository, CompanyRepository companyRepository) {
        this.receivedDocumentFromClientRepository = receivedDocumentFromClientRepository;
        this.companyRepository = companyRepository;
    }
    @Transactional
    public void saveReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idReceivedDocumentFromClientStatus) {
        Company company = companyRepository.findById(idCompany)
                .orElseThrow(() -> new RuntimeException("No firms for this Id"));
        receivedDocumentFromClient.setIdCompany(idCompany);
        receivedDocumentFromClient.setData(now().minusMonths(1).toString().substring(0, 7));
        receivedDocumentFromClient.setIdReceivedDocumentFromClientStatus(idReceivedDocumentFromClientStatus);
        receivedDocumentFromClient.setNumber(company.getRaksNumber());
        receivedDocumentFromClientRepository.save(receivedDocumentFromClient);
    }

    @Transactional
    public void editReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idReceivedDocumentFromClientStatus)  {
        receivedDocumentFromClient.setIdReceivedDocumentFromClientStatus(idReceivedDocumentFromClientStatus);
        receivedDocumentFromClient.setData(now().minusMonths(1).toString().substring(0, 7));
        receivedDocumentFromClientRepository.save(receivedDocumentFromClient);
    }

}
