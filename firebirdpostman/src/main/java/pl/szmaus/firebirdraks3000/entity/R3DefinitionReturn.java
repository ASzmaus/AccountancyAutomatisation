package pl.szmaus.firebirdraks3000.entity;

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
@Table(name="R3_DEFINICJE_DEKLARACJI")

public class R3DefinitionReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    private Integer id;
    @Column(name = "NAZWA")
    private String name;
    @Column(name="XML_EDEKLARACJI")
    private byte[] xmlEReturn;


    @Override
    public String toString() {
        return "Definition declaration [id=" + id ;
    }
}
