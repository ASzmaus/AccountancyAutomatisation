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
@Table(name="FI_REJESTR_VAT_POZ")

public class FiRegisterVatOther {
    @Id
    @Column(name="ID")
    private Long id;
    @Column(name="VAT_ID")
    private Long vatId;
    @Column(name="VAT_RATE_ID")
    private Long vatRateId;
    @Column(name="VAT_RATE_VALUE")
    private String vatRateValue;
    @Column(name="AMOUNT_NETTO")
    private BigDecimal amountNet;
    @Column(name="AMOUNT_VAT")
    private BigDecimal amuntVat;
    @Column(name="AMOUNT_BRUTTO")
    private BigDecimal amountGross;
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
    @Column(name="COR_TYPE")
    private String coreType;


}
