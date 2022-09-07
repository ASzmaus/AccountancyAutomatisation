package pl.szmaus.firebirdf00152.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name="R3_CONTACTS")

public class R3Contact {
    @Id
    @Column(name="ID")
    private Long id;
    @Column(name="SHORT_NAME")
    private String shortName;
    @Column(name="TAXID")
    private String taxId;
    @Column(name="FULL_NAME")
    private String fullName;
    @Column(name="ZIPCODE")
    private String zipCode;
    @Column(name="EU_CODE")
    private String euCode;
    @Column(name="PLACE")
    private  String place;
    @Column(name="STREET")
    private  String street;
    @Column(name="BUILDING_NUMBER")
    private String buildingNumber;
    @Column(name="APARTMENT_NUMBER")
    private String apartmentNumber;


}
