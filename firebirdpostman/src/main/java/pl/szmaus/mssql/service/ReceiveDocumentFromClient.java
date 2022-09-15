package pl.szmaus.mssql.service;

import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;

import java.util.List;

public interface ReceiveDocumentFromClient {

        public void saveReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idReceivedDocumentFromClientStatus);

        public void editReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idReceivedDocumentFromClientStatus);

        public void saveStatusForDocuments(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idStatusDocuments);

        public Boolean ifNotReceivedDocumentFirstInfo(ReceivedDocumentFromClient receivedDocumentFromClient);

        public Boolean ifNotReceivedDocumentFirstReminder(ReceivedDocumentFromClient receivedDocumentFromClient);

        public Boolean ifNotReceivedDocumentSecondReminder(ReceivedDocumentFromClient receivedDocumentFromClient);

        public void checkStatusForDocuments(ReceivedDocumentFromClient receivedDocumentFromClient, List<Company> companyList, OtherRemainingFile otherRemainingFile);
}
