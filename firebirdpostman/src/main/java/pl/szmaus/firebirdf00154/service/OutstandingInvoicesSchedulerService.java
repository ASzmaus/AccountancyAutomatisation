package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.Enum.InvoiceStatus;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.command.SalesInvoiceCommand;
import pl.szmaus.firebirdf00154.mapper.SalesInvoiceMapper;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.firebirdf00154attachment.repository.R3DocumentFilesRepository;
import pl.szmaus.utility.MailsUtility;
import java.time.LocalDate;
import java.util.List;
import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class OutstandingInvoicesSchedulerService extends AbstractMailDetails  {

    private final GetSalesInvoice getSalesInvoice;
    private final R3DocumentFilesRepository r3DocumentFilesRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    public OutstandingInvoicesSchedulerService(
        GetSalesInvoice getSalesInvoice,
        R3DocumentFilesRepository r3DocumentFilesRepository,
        ScheduleConfiguration scheduleConfiguration, SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany,
        SalesInvoiceMapper salesInvoiceMapper){
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.getSalesInvoice = getSalesInvoice;
        this.r3DocumentFilesRepository = r3DocumentFilesRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
    }

    @Scheduled(cron = "${scheduling.cronOutstandingInvoice}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            getSalesInvoice.issuedInvoicesList(now().getMonth(), now().getYear())
                    .stream()
                    .filter(p -> r3DocumentFilesRepository.findByGuid(p.getGuid()) != null && p.getStatus() != null)
                    .forEach(d -> {
                        SalesInvoiceCommand salesInvoiceCommand= salesInvoiceMapper.mapSalesInvoiceToSalesInvoiceCommand(d);
                        List<Company> companyList = getCompany.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        String toEmail= mailConfiguration.getBlockToEmailProd().equals(false) ? getCompany.returnCompanyEmails(companyList.get(0)) : mailConfiguration.getToEmail();
                        if (isNotPaidInvoiceBeforeDeadline(d)) {
                            mailDetails = MailsUtility.createMailDetails(
                                    "Upływający termin płatności faktury nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminder1.mustache",salesInvoiceCommand) + footer,
                                    mailConfiguration.getBccEmail(), toEmail);
                            getSalesInvoice.setStatusInvoice(d, InvoiceStatus.START_SENDING_REMAINDER1.label);
                        } else if (isNotPaidInvoiceAfterDeadline(d)) {
                            mailDetails = MailsUtility.createMailDetails(
                                    "Informujemy o braku wpłaty za wystawioną fakturę nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminder2.mustache",salesInvoiceCommand) + footer,
                                    mailConfiguration.getBccEmail(), toEmail);
                            getSalesInvoice.setStatusInvoice(d, InvoiceStatus.START_SENDING_REMAINDER2.label);
                        }
                        if (isNotPaidInvoiceBeforeDeadline(d) || isNotPaidInvoiceAfterDeadline(d)) {
                            sendEmailMicrosoft.configurationMicrosoft365Email(
                                    mailDetails.getToEmail(),
                                    mailDetails.getBccEmail(),
                                    mailDetails.getMailBody(),
                                    mailDetails.getMailTitle());
                            log4J2PropertiesConf.logSentMail(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                            if (isNotPaidInvoiceBeforeDeadline(d)) {
                                getSalesInvoice.setStatusInvoice(d, InvoiceStatus.REMAINDER1.label);
                            } else if (isNotPaidInvoiceAfterDeadline(d)) {
                                getSalesInvoice.setStatusInvoice(d, InvoiceStatus.REMAINDER2.label);
                            }
                        }
                    });
        } catch (Exception e) {
            log4J2PropertiesConf.logSendingInv(mailDetails.getMailTitle(), e);
        }
    }

    private Boolean isNotPaidInvoiceAfterDeadline(SalesInvoice salesInvoice) {
        return  now().isAfter(dateRefersToPaymentDeadline()) && isStatusInvoiceIsAfterDeadline(salesInvoice);
         }
    private LocalDate dateRefersToPaymentDeadline(){
        return LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getPaymentDate());
    }

    private Boolean isStatusInvoiceIsAfterDeadline(SalesInvoice salesInvoice){
        return salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER2.label) || salesInvoice.getStatus().equals(InvoiceStatus.REMAINDER1.label);
    }

    private Boolean isNotPaidInvoiceBeforeDeadline(SalesInvoice salesInvoice) {
        return now().isAfter(dateBeforeInvoiceReminder()) && now().isBefore(dateRefersToPaymentDeadline()) && isStatusInvoiceIsBeforeDeadline(salesInvoice);
    }

    private LocalDate dateBeforeInvoiceReminder(){
        return LocalDate.of(now().getYear(), now().getMonth(),scheduleConfiguration.getPaymentDayInInvoiceReminder()).minusDays(1);
    }
    private Boolean isStatusInvoiceIsBeforeDeadline(SalesInvoice salesInvoice){
        return salesInvoice.getStatus().equals(InvoiceStatus.SENDING_INVOICE.label) || salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER1.label);
    }
}