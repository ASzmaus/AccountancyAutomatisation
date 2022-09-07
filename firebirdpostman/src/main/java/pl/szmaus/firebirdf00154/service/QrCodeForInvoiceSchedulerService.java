package pl.szmaus.firebirdf00154.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.utility.MailsUtility;
import java.io.IOException;
import java.io.StringWriter;
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

    @Transactional
    @Override
    @Scheduled(cron = "${scheduling.cronQRCode}")
    public void trackSendEmail() {
            salesInvoiceService.issuedInvoicesList(now().getMonth(),now().getYear())
                    .stream()
                    .forEach(d -> {
                        List<Company> companyList = super.getCompanyService().findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        String toEmail = "";
                        if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                            toEmail = super.getMailConfiguration().getToEmail();
                        } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                            toEmail = super.getMailConfiguration().getToEmail();
                        }
                        super.setMailDetails(MailsUtility.createMailDetails("Wprowadzamy nową funkcjonalność ułatwiającą płatności " + companyList.get(0).getShortName(),
                                super.executeAndCompileMustacheTemplate("template/QRqode.mustache",d) + super.getFooter(),
                                        super.getMailConfiguration().getBccEmail(), toEmail, super.getEmailAttachment(), super.getImagesMap()));

                        super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), null);
                        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
                        log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
                    });
    }
}
