package pl.szmaus.firebirdf00154.entity;

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
@Table(name = "FI_KART_INNE_POZ")
public class OtherRemainingFile { // class which keeps info regarding received documents from clients - raksNumber (idCompany from administration module in raks) and name - for what month received docouments in accountant office
	@Id
	@Column(name = "ID")
	private Integer id;
	@Column(name = "ID_INNE_KART")
	private Integer idTypeOtherFile;
	@Column(name = "NUMER")
	private String number;
	@Column(name = "NAZWA")
	private String name;
	@Column(name = "WALUTA")
	private String currency;
	@Column(name = "OPIS")
	private String	description;

	@Override
	public String toString() {
		return "OtherRemainingFile [id" + id+ "idTypeOtherFile"+ idTypeOtherFile+ "number=" + number +", name=" + name + "currency" + currency +"description"+description+"]";
	}


}
