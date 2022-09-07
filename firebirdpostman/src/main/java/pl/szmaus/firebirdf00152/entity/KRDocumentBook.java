package pl.szmaus.firebirdf00152.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="KR_DOKUMENTY_KSIEGA")

public class KRDocumentBook {
    @Id
    @Column(name="ID", nullable = false)
    private Long id;
    @Column(name="ID_ROKU")
    private Long IdYear;
    @Column(name="ACCOUNT_DOC_ID")
    private Long accountDocId;
    @Column(name="NR_KSIEGI")
    private Long nrBook;
    @Column(name="TYP_ZAPISU")
    private Long recordType;
    @Column(name="KWOTA")
    private BigDecimal amount;
    @Column(name="KWOTA_DODATKOWA")
    private BigDecimal amountAdditional;
    @Column(name="OPIS_OPERACJI")
    private String descriptionOfOperation;
    @Column(name="IMPORTED")
    private Boolean imported;
    @Column(name="EDIT_TIME")
    private Long editTime;
    @Column(name="C_IDENT")
    private String cIdentUser;
    @Column(name="M_IDENT")
    private String mIdent;
    @Column(name="C_DATE")
    private LocalDateTime cDate;
    @Column(name="M_DATE")
    private LocalDateTime mDate;
    @Column(name="KWOTA_WAL")
    private BigDecimal amountCurrency;
    @Column(name="KWOTA_DOD_WAL")
    private BigDecimal amountAddCurrency;
    @Column(name="WALUTA")
    private String currency;
    @Column(name="WALUTA_DODATKOWA")
    private String currencyAdditional;


}
