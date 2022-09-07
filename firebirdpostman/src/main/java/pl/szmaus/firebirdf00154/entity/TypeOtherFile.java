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
@Table(name = "FI_KART_INNE")
public class TypeOtherFile{ // class which keeps info regarding type of other remaining file reg. receiving documents from clients
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Integer id;
	@Column(name = "NAZWA")
	private String name;
	@Column(name = "OPIS")
	private String	description;

}
