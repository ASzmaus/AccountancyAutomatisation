package pl.szmaus.firebirdraks3000.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="R3_JPK")

public class R3Jpk {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    private Long id;
    @Column(name = "ID_FIRMY")
    private Integer idOwner;
    @Column(name="PLIK")
    private byte[] file;
    @Column(name="OPIS")
    private String description;
    @Column(name="STATUS")
    private Short status;
    @Column(name="ID_DEKLARACJI")
    private Integer idR3Return;


    @Override
    public String toString() {
        return "JPK[id=" + id + idOwner+file;
    }
}
