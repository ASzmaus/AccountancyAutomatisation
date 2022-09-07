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
@Table(name = "e_accountant_status")
public class EAccountantStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Integer id;
    @Column(name =  "Content")
    private String content;

    @Override
    public String toString() {
        return "Content tabel [" + "id=" + id + ", content=" + content +"]";
    }
}
