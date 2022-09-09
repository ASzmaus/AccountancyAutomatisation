package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.utility.MailsUtility;
import java.util.Calendar;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class ReminderForClientSchedulerService extends AbstractMailDetails {

    public ReminderForClientSchedulerService(ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
    }

    @Scheduled(cron = "${scheduling.cronReminderForClient}")
    public void trackSendEmail() {
        final Calendar calendar = Calendar.getInstance();
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
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
                    "Faktury sprzedażowe w Raks spółka NazwaSpółki",
                    executeAndCompileMustacheTemplate("template/invoiceReminderForClient.mustache",this) + footer,
                    bccEmail, toEmail);
            try {
                sendingEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
                log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
            } catch (Exception e) {
                log4J2PropertiesConf.performSendingInv(mailDetails.getMailTitle(), e);
            }
        }
    }
}
