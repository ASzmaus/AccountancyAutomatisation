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

    public ReminderForClientSchedulerService( ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
    }

    @Override
    @Scheduled(cron = "${scheduling.cronReminderForClient}")
    public void trackSendEmail() {
        final Calendar calendar = Calendar.getInstance();
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
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
                    "Faktury sprzedażowe w Raks spółka NazwaSpółki",
                    super.executeAndCompileMustacheTemplate("template/invoiceReminderForClient.mustache",this) + super.getFooter(),
                    bccEmail, toEmail, null, null));
            try {
                super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
                log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
            } catch (Exception e) {
                log4J2PropertiesConf.performSendingInv(super.getMailDetails().getMailTitle(), e);
            }
        }
    }
}
