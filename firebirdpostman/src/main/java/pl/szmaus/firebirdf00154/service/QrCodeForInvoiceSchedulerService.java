package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.utility.MailsUtility;
import java.util.List;
import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class QrCodeForInvoiceSchedulerService extends AbstractMailDetails {

    private final GetSalesInvoice getSalesInvoice;

    public QrCodeForInvoiceSchedulerService(
        GetSalesInvoice getSalesInvoice,
        ScheduleConfiguration scheduleConfiguration,
        SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.getSalesInvoice = getSalesInvoice;
    }

    @Scheduled(cron = "${scheduling.cronQRCode}")
    public void trackSendEmail() {
            getSalesInvoice.issuedInvoicesList(now().getMonth(),now().getYear())
                    .stream()
                    .forEach(d -> {
                        List<Company> companyList = getCompany.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        String toEmail= mailConfiguration.getBlockToEmailProd().equals(false) ? companyList.get(0).getFirmEmailAddress() : mailConfiguration.getToEmail();
                        mailDetails = MailsUtility.createMailDetails("Wprowadzamy nową funkcjonalność ułatwiającą płatności " + companyList.get(0).getShortName(),
                                executeAndCompileMustacheTemplate("template/QRqode.mustache",d) + footer,
                                        mailConfiguration.getBccEmail(), toEmail);

                        sendEmailMicrosoft.configurationMicrosoft365Email(
                                mailDetails.getToEmail(),
                                mailDetails.getBccEmail(),
                                mailDetails.getMailBody(),
                                mailDetails.getMailTitle());
                        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
                        log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                    });
    }
}
