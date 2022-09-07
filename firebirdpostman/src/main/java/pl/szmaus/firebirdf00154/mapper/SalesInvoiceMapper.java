package pl.szmaus.firebirdf00154.mapper;

import org.springframework.stereotype.Component;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.command.SalesInvoiceCommand;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class SalesInvoiceMapper {

    private final ScheduleConfiguration scheduleConfiguration;
    private final MailConfiguration mailConfiguration;
    private static final String PAYMENT_METHOD_CASH = "Gotówka";

    public SalesInvoiceMapper(ScheduleConfiguration scheduleConfiguration, MailConfiguration mailConfiguration) {
        this.scheduleConfiguration = scheduleConfiguration;
        this.mailConfiguration = mailConfiguration;
    }

    public SalesInvoiceCommand mapSalesInvoiceToSalesInvoiceCommand(SalesInvoice salesInvoice){
        return SalesInvoiceCommand
                .builder()
                .number(salesInvoice.getNumber())
                .fullNameReceiver(salesInvoice.getFullNameReceiver())
                .issueInvoiceDate(salesInvoice.getIssueInvoiceDate())
                .grossAmountInPln(salesInvoice.getGrossAmountInPln().setScale(2, RoundingMode.CEILING).toString().replace(".", ","))
                .dueDate(salesInvoice.getIssueInvoiceDate().plusDays(scheduleConfiguration.getPaymentDate()))
                .nameOfPayment(salesInvoice.getNameOfPayment().equals(PAYMENT_METHOD_CASH) ? salesInvoice.getNameOfPayment() : salesInvoice.getNameOfPayment() +" " + mailConfiguration.getOfficeBankAccount())
                .currentDate(LocalDate.now())
                .lastReminderDate(LocalDate.now().plusDays(scheduleConfiguration.getLastReminderDay()))
                .build();
    }
}
