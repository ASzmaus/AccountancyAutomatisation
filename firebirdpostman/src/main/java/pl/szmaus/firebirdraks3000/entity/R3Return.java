package pl.szmaus.firebirdraks3000.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="R3_DEKLARACJE")

public class R3Return {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="ID")
    private Integer id;
    @Column(name = "DATA_DEKLARACJI")
    private LocalDate returnDate;
    @Column(name = "ID_DEFINICJI_DEKLARACJI")
    private Integer id_definition_return;
    @Column(name="ID_WLASCICIELA")
    private  Integer idOwner;
    @Column (name="NIP")
    private String nip;
    @Column (name="NAZWA_WLASCICIELA")
    private String nameOwner;
    @Column(name="PODATEK")
    private Integer tax;
    @Column(name="DANE_DEKLARACJI")
    private byte[] dataOfReturns;
    @Column(name="EDEKL_STAN_PRZETWARZANIA")
    private Integer eReturnStatusProcess;
    @Column(name="EDEKL_WYSLANY_DOKUMENT")
    private  byte[] eReturnSendDocument;
    @Column(name="EMAIL_WYSLANO")
    private Boolean emailSent;
    @Column(name="EMAIL_DATA_WYSLANIA")
    private LocalDate emailDataSent;
    @Column(name="C_DATE")
    private LocalDate createData;


    @Override
    public String toString() {
        return "return [id=" + id + ", podatek=" + tax + ", name=" + nameOwner + ", date=" + getReturnDate()+ "data from return in blob" + dataOfReturns+"]";
    }
}
