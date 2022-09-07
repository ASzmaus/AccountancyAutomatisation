package pl.szmaus.firebirdraks3000.command;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class R3ReturnCommand {

	private String settlementPeriod;
	private Integer tax;
	private LocalDate dueDatePit;
	private LocalDate dueDateRyczalt;
	private LocalDate dueDateCit;
	private LocalDate dueDateVat;
	private String nameOwner;
	private String vatOverPayment;
}
