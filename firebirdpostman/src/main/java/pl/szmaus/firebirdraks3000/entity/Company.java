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
@Table(name = "AD_firms")
public class Company {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	@Column(name = "number")
	private Integer raksNumber; // number in administration module in accounting system Raks)
	@Column(name = "shortname")
	private String shortName;
	@Column(name = "firm_email_address")
	private String	firmEmailAddress;

	@Column(name = "firm_phone_numbers")
	private String	firmPhoneNumbers;
	@Column(name = "taxid")
	private String taxId;

	@Override
	public String toString() {
		return "AD_Firms [id=" + id +  "raksNumber=" + raksNumber +", shortname=" + shortName + ", firmEmailAddress=" + firmEmailAddress + "taxId"+ taxId+"]";
	}
}
