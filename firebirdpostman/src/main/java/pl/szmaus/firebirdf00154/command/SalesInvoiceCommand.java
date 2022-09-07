package pl.szmaus.firebirdf00154.command;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class SalesInvoiceCommand {
	private String number;
	private String fullNameReceiver;
	private LocalDate issueInvoiceDate;
	private String grossAmountInPln;
	private LocalDate dueDate;
	private String payment;
	private String nameOfPayment;
	private LocalDate currentDate;
	private LocalDate lastReminderDate;

}
