package pl.szmaus.mssql.service;

import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;

public interface ReceiveDocumentFromClient {

        public void saveReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idReceivedDocumentFromClientStatus);

        public void editReceivedDocumentFromClient(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idReceivedDocumentFromClientStatus);
}
