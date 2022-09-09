package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.utility.MailsUtility;
import java.util.List;
import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class QrCodeForInvoiceSchedulerService extends AbstractMailDetails {

    private final SalesInvoiceService salesInvoiceService;

    public QrCodeForInvoiceSchedulerService(SalesInvoiceService salesInvoiceService, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
       super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceService = salesInvoiceService;
    }

    @Scheduled(cron = "${scheduling.cronQRCode}")
    public void trackSendEmail() {
            salesInvoiceService.issuedInvoicesList(now().getMonth(),now().getYear())
                    .stream()
                    .forEach(d -> {
                        List<Company> companyList = companyService.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        String toEmail = "";
                        if (mailConfiguration.getBlockToEmailProd().equals(false)) { //prod
                            toEmail = mailConfiguration.getToEmail();
                        } else if (mailConfiguration.getBlockToEmailProd().equals(true)) { // dev
                            toEmail = mailConfiguration.getToEmail();
                        }
                        mailDetails =MailsUtility.createMailDetails("Wprowadzamy nową funkcjonalność ułatwiającą płatności " + companyList.get(0).getShortName(),
                                executeAndCompileMustacheTemplate("template/QRqode.mustache",d) + footer,
                                        mailConfiguration.getBccEmail(), toEmail);

                        sendingEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(),mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), null);
                        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
                        log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                    });
    }
}
