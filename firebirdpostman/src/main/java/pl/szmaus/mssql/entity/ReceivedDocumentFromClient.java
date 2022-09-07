package pl.szmaus.mssql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "received_document_from_client")
public class ReceivedDocumentFromClient {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;
    @Column(name = "Id_company")
    private Integer idCompany;
    @Column(name = "Number")
    private Integer number;
    @Column(name = "Id_received_document_from_client_status")
    private Integer idReceivedDocumentFromClientStatus;
    @Column(name = "Date")
    private String data;

    @Override
    public String toString() {
        return "Contact tabela ["  + "number=" + number +   "idCompany=" + idCompany + ", idReceivedDocumentFromClientStatus=" + idReceivedDocumentFromClientStatus +"]";
    }

}
