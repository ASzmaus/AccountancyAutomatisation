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
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.firebirdf00154attachment.repository.R3DocumentFilesRepository;
import pl.szmaus.utility.MailsUtility;
import java.time.LocalDate;
import java.util.List;
import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class OutstandingInvoicesSchedulerService extends AbstractMailDetails  {

    private final SalesInvoiceService salesInvoiceService;
    private final R3DocumentFilesRepository r3DocumentFilesRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    public OutstandingInvoicesSchedulerService(SalesInvoiceService salesInvoiceService, R3DocumentFilesRepository r3DocumentFilesRepository, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService, SalesInvoiceMapper salesInvoiceMapper){
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceService = salesInvoiceService;
        this.r3DocumentFilesRepository = r3DocumentFilesRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
    }

    @Scheduled(cron = "${scheduling.cronOutstandingInvoice}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
        if(now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getPaymentDayInInvoiceReminder()).minusDays(1))  || now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getPaymentDate()).plusDays(1))) {
            String toEmail = "";
            String bccEmail = "";
            if (mailConfiguration.getBlockToEmailProd().equals(false)) { //prod
                toEmail = mailConfiguration.getToEmailClient();
                bccEmail = mailConfiguration.getBccEmailClient();
            } else if (mailConfiguration.getBlockToEmailProd().equals(true)) { // dev
                toEmail = mailConfiguration.getToEmail();
                bccEmail = mailConfiguration.getBccEmail();
            }
            mailDetails =MailsUtility.createMailDetails(
                    "Sprawdź statusy opłacenia faktur sprzedażowych w Raks spółka NazwaSpółki",
                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminderForClient.mustache",this) + footer,
                    bccEmail, toEmail);
            sendingEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(),mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
            log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
        }else {
            salesInvoiceService.issuedInvoicesList(now().getMonth(), now().getYear())
                    .stream()
                    .filter(p -> r3DocumentFilesRepository.findByGuid(p.getGuid()) != null && p.getStatus() != null)
                    .forEach(d -> {
                        String toEmail = "";
                        SalesInvoiceCommand salesInvoiceCommand= salesInvoiceMapper.mapSalesInvoiceToSalesInvoiceCommand(d);
                        List<Company> companyList = companyService.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        if (mailConfiguration.getBlockToEmailProd().equals(false)) { //prod
                            toEmail = companyList.get(0).getFirmEmailAddress();
                        } else if (mailConfiguration.getBlockToEmailProd().equals(true)) { // dev
                            toEmail = mailConfiguration.getToEmail();
                        }
                        if (ifNotPaidInvoiceBeforeDeadline(d)) {
                            mailDetails = MailsUtility.createMailDetails(
                                    "Upływający termin płatności faktury nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminder1.mustache",salesInvoiceCommand) + footer,
                                    mailConfiguration.getBccEmail(), toEmail);
                            salesInvoiceService.setStatusInvoice(d, InvoiceStatus.START_SENDING_REMAINDER1.label);
                        } else if (ifNotPaidInvoiceAfterDeadline(d)) {
                            mailDetails = MailsUtility.createMailDetails(
                                    "Informujemy o braku wpłaty za wystawioną fakturę nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminder2.mustache",salesInvoiceCommand) + footer,
                                    mailConfiguration.getBccEmail(), toEmail);
                            salesInvoiceService.setStatusInvoice(d, InvoiceStatus.START_SENDING_REMAINDER2.label);
                        }
                        if (ifNotPaidInvoiceBeforeDeadline(d) || ifNotPaidInvoiceAfterDeadline(d)) {
                            sendingEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(),mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
                            log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                            if (ifNotPaidInvoiceBeforeDeadline(d)) {
                                salesInvoiceService.setStatusInvoice(d, InvoiceStatus.REMAINDER1.label);
                            } else if (ifNotPaidInvoiceAfterDeadline(d)) {
                                salesInvoiceService.setStatusInvoice(d, InvoiceStatus.REMAINDER2.label);
                            }
                        }
                    });
        }
        } catch (Exception e) {
            log4J2PropertiesConf.performSendingInv(mailDetails.getMailTitle(), e);
        }
    }

    private Boolean ifNotPaidInvoiceAfterDeadline(SalesInvoice salesInvoice) {
        return  now().isAfter(LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getPaymentDayInInvoice()))
                && (salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER2.label) || salesInvoice.getStatus().equals(InvoiceStatus.REMAINDER1.label));
         }

    private Boolean ifNotPaidInvoiceBeforeDeadline(SalesInvoice salesInvoice) {
        return now().isAfter(LocalDate.of(now().getYear(), now().getMonth(), scheduleConfiguration.getPaymentDayInInvoiceReminder()).minusDays(1))
        && now().isBefore(LocalDate.of(now().getYear(), now().getMonth(), scheduleConfiguration.getPaymentDayInInvoice()))
        && (salesInvoice.getStatus().equals(InvoiceStatus.SENDING_INVOICE.label) || salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER1.label));
    }
}