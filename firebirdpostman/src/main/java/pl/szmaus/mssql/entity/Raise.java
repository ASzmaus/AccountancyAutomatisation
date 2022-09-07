package pl.szmaus.mssql.entity;

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
@Table(name = "raise")
public class Raise {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	@Column(name = "raks_number")
	private Integer	raksNumber;
	@Column(name = "base_amount")
	private Double baseAmount;
	@Column(name = "raise_in_pln")
	private Double raiseInPln;
	@Column(name = "new_price")
	private Double newPrice;
	@Column(name = "raise_in_percentage")
	private Double raiseInPercentage;
	@Column(name = "id_raise_status")
	private Integer	idRaiseStatus;
	@Column(name = "email")
	private String	email;
	@Column(name = "raise_date")
	private LocalDate raiseDate;

}
