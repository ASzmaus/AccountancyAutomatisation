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
import pl.szmaus.firebirdf00154.repository.SalesInvoiceRepository;
import pl.szmaus.firebirdf00154attachment.repository.R3DocumentFilesRepository;
import pl.szmaus.utility.MailsUtility;
import java.time.LocalDate;
import java.util.List;
import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class OutstandingInvoicesSchedulerService extends AbstractMailDetails  {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceService salesInvoiceService;
    private final R3DocumentFilesRepository r3DocumentFilesRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    public OutstandingInvoicesSchedulerService(SalesInvoiceRepository salesInvoiceRepository, SalesInvoiceService salesInvoiceService, R3DocumentFilesRepository r3DocumentFilesRepository, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService, SalesInvoiceMapper salesInvoiceMapper){
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesInvoiceService = salesInvoiceService;
        this.r3DocumentFilesRepository = r3DocumentFilesRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
    }

    @Override
    @Scheduled(cron = "${scheduling.cronOutstandingInvoice}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
        if(now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getPaymentDayInInvoiceReminder()).minusDays(1))  || now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getPaymentDate()).plusDays(1))) {
            String toEmail = "";
            String bccEmail = "";
            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                toEmail = super.getMailConfiguration().getToEmailClient();
                bccEmail = super.getMailConfiguration().getBccEmailClient();
            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                toEmail = super.getMailConfiguration().getToEmail();
                bccEmail = super.getMailConfiguration().getBccEmail();
            }
            super.setMailDetails(MailsUtility.createMailDetails(
                    "Sprawdź statusy opłacenia faktur sprzedażowych w Raks spółka NazwaSpółki",
                    super.executeAndCompileMustacheTemplate("template/outstandingDebtsReminderForClient.mustache",this) + super.getFooter(),
                    bccEmail, toEmail, null, null));
            super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
            log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
        }else {
            salesInvoiceService.issuedInvoicesList(now().getMonth(), now().getYear())
                    .stream()
                    .filter(p -> r3DocumentFilesRepository.findByGuid(p.getGuid()) != null && p.getStatus() != null)
                    .forEach(d -> {
                        String toEmail = "";
                        SalesInvoiceCommand salesInvoiceCommand= salesInvoiceMapper.mapSalesInvoiceToSalesInvoiceCommand(d);
                        List<Company> companyList = super.getCompanyService().findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                            toEmail = companyList.get(0).getFirmEmailAddress();
                        } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                            toEmail = super.getMailConfiguration().getToEmail();
                        }
                        if (ifNotPaidInvoiceBeforeDeadline(d)) {
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    "Upływający termin płatności faktury nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    super.executeAndCompileMustacheTemplate("template/outstandingDebtsReminder1.mustache",salesInvoiceCommand) + super.getFooter(),
                                    super.getMailConfiguration().getBccEmail(), toEmail, null, null));
                            d.setStatus(InvoiceStatus.START_SENDING_REMAINDER1.label);
                            salesInvoiceRepository.save(d);
                        } else if (ifNotPaidInvoiceAfterDeadline(d)) {
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    "Informujemy o braku wpłaty za wystawioną fakturę nr " + " " + d.getNumber() + " za usługi opieki księgowej",
                                    super.executeAndCompileMustacheTemplate("template/outstandingDebtsReminder2.mustache",salesInvoiceCommand) + super.getFooter(),
                                    super.getMailConfiguration().getBccEmail(), toEmail, null, null));
                            d.setStatus(InvoiceStatus.START_SENDING_REMAINDER2.label);
                            salesInvoiceRepository.save(d);
                        }
                        if (ifNotPaidInvoiceBeforeDeadline(d) || ifNotPaidInvoiceAfterDeadline(d)) {
                            super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
                            log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
                            if (ifNotPaidInvoiceBeforeDeadline(d)) {
                                d.setStatus(InvoiceStatus.REMAINDER1.label);
                                salesInvoiceRepository.save(d);
                            } else if (ifNotPaidInvoiceAfterDeadline(d)) {
                                d.setStatus(InvoiceStatus.REMAINDER2.label);
                                salesInvoiceRepository.save(d);
                            }
                        }
                    });
        }
        } catch (Exception e) {
            log4J2PropertiesConf.performSendingInv(super.getMailDetails().getMailTitle(), e);
        }
    }

    private Boolean ifNotPaidInvoiceAfterDeadline(SalesInvoice salesInvoice) {
        return  now().isAfter(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getPaymentDayInInvoice()))
                && (salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER2.label) || salesInvoice.getStatus().equals(InvoiceStatus.REMAINDER1.label));
         }

    private Boolean ifNotPaidInvoiceBeforeDeadline(SalesInvoice salesInvoice) {
        return now().isAfter(LocalDate.of(now().getYear(), now().getMonth(), super.getScheduleConfiguration().getPaymentDayInInvoiceReminder()).minusDays(1))
        && now().isBefore(LocalDate.of(now().getYear(), now().getMonth(), super.getScheduleConfiguration().getPaymentDayInInvoice()))
        && (salesInvoice.getStatus().equals(InvoiceStatus.SENDING_INVOICE.label) || salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_REMAINDER1.label));
    }
}