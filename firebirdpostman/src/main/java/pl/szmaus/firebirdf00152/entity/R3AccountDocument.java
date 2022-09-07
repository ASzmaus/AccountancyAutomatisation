package pl.szmaus.firebirdf00152.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.GenerationType.TABLE;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="R3_ACCOUNT_DOCUMENTS")

public class R3AccountDocument{
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name="DOC_KIND_ID")
    private Long docKindId;
    @Column(name="DOCUMENT_NUMBER")
    private String documentNumber;
    @Column(name="DOCUMENT_DATE")
    private LocalDate documentDate;
    @Column(name="C_IDENT")
    private String cIdentUser;
    @Column(name="M_IDENT")
    private String mIdentUser;
    @Column(name="C_DATE")
    private LocalDateTime cDate;
    @Column(name="M_DATE")
    private LocalDateTime mDate;
    @Column(name="ACCOUNT_CODE_ID")
    private Long accountCodeId;
    @Column(name="ACCOUNT_CODE")
    private String accountCode;
    @Column(name="ACCOUNT_DATE")
    private LocalDate accountDate;
    @Column(name="ACCOUNT_NUM")
    private Integer accountNumber;
    @Column(name="ACCOUNT_DOC_STATE")
    private String accountDocState;
    @Column(name="ACCOUNT_NUM_FULL")
    private String accountNumFull;
    @Column(name="ACCOUNT_DOC_CLOSED")
    private Short accountDocClosed;
    @Column(name="CONTACT_ID")
    private Long contactId;
    @Column(name="CONTACT_NAME")
    private String contactName;
    @Column(name="CONTACT_FULL_NAME")
    private String contactFullName;
    @Column(name="CONTACT_NIP")
    private String contactNip;
    @Column(name="CONTACT_ZIPCODE")
    private String contactZipCode;
    @Column(name="CONTACT_EUCODE")
    private String contactEuCode;
    @Column(name="CONTACT_PLACE")
    private String contactPlace;
    @Column(name="CONTACT_ADDRESS")
    private String contactAddress;
    @Column(name="CONTACT_BUILDING_NUMBER")
    private String contactBuildingNumber;
    @Column(name="CONTACT_APARTMENT_NUMBER")
    private String contactApartmentNumber;
    @Column(name="DOC_GROUP_ID")
    private Long docGroupId;
    @Column(name="ZAPIS_KR")
    private Boolean zapisKr;
    @Column(name="IMPORTED")
    private Boolean ifImported;
    @Column(name="ZAPIS_RS")
    private Boolean zapisRs;
    @Column(name="DOCUMENT_OP_DATE")
    private LocalDate documentOpDate;

}
