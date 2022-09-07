package pl.szmaus.mssql.entity;

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
@Table(name = "e_accountant")
public class EAccountant {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "Id")
	private Integer id;
	@Column(name = "Raks_no")
	private Integer raksNo;
	@Column(name = "Name")
	private String name;
	@Column(name = "Surname")
	private String surname;
	@Column(name = "Company_name")
	private String companyName;
	@Column(name = "Email")
	private String	email;
	@Column(name = "Password")
	private String	password;
	@Column(name = "Id_e_accountant_status")
	private Integer	idEAccountantStatus;



	@Override
	public String toString() {
		return "eAccountant [id=" + id + ", raksno=" + raksNo + ", name=" + name + ", surnamename=" + surname + ", email=" + email  + ", status=" + idEAccountantStatus +"]";
	}
}
