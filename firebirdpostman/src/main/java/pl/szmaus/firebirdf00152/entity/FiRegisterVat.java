package pl.szmaus.firebirdf00152.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="FI_REJESTR_VAT")

public class FiRegisterVat {
    @Id
    @Column(name="ID")
    private Long id;
    @Column(name="ACCOUNT_DOC_ID")
    private Long accountDocId;
    @Column(name="VAT_TYPE")
    private String vatType;
    @Column(name="VAT_REGISTER")
    private String vatRegister;
    @Column(name="VAT_COLUMN")
    private Long vatColumn;
    @Column(name="IMPORTED")
    private Boolean imported;
    @Column(name="EDIT_TIME")
    private Long editTime;
    @Column(name="C_IDENT")
    private String cIdentUser;
    @Column(name="M_IDENT")
    private String MIdentUser;
    @Column(name="C_DATE")
    private LocalDateTime cDate;
    @Column(name="M_DATE")
    private LocalDateTime mDate;
    @Column(name="VAT_REGISTER_ID")
    private Long vatRegisterId;
    @Column(name="VAT_DATE")
    private LocalDate vatDate;
    @Column(name="RECEIVE_DATE")
    private LocalDate receiveDate;
    @Column(name="SALE_DATE")
    private LocalDate saleDate;
    @Column(name = "SPRZEDAZ_BRUTTO_MARZA")
    private BigDecimal salesGrossMargin;
    @Column(name = "ZAKUP_BRUTTO_MARZA")
    private BigDecimal purchaseGrossMargin;

}
