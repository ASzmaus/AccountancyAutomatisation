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
@Table(name = "received_document_from_client_status")
public class ReceivedDocumentFromClientStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;
    @Column(name =  "Content")
    private String content;

    @Override
    public String toString() {
        return "Content tabela [" + "id=" + id + ", icontent=" + content +"]";
    }
}
