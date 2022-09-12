package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.utility.MailsUtility;

import java.time.LocalDate;
import java.util.Calendar;

import static java.time.LocalDate.now;


@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class ReminderForClientSchedulerService extends AbstractMailDetails {

    public ReminderForClientSchedulerService(ScheduleConfiguration scheduleConfiguration, SendEmailMicrosoft sendEmailMicrosoft, MailConfiguration mailConfiguration, GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
    }

    @Scheduled(cron = "${scheduling.cronReminderForClient}")
    public void trackSendEmail() {
        final Calendar calendar = Calendar.getInstance();
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            String toEmail = "";
            String bccEmail = "";
            if (mailConfiguration.getBlockToEmailProd().equals(false)) { //prod
                toEmail = mailConfiguration.getToEmailClient();
                bccEmail = mailConfiguration.getBccEmailClient();
            } else if (mailConfiguration.getBlockToEmailProd().equals(true)) { // dev
                toEmail = mailConfiguration.getToEmail();
                bccEmail = mailConfiguration.getBccEmail();
            }
        if (calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE)) {
            mailDetails =MailsUtility.createMailDetails(
                    "Faktury sprzedażowe w Raks spółka NazwaSpółki",
                    executeAndCompileMustacheTemplate("template/invoiceReminderForClient.mustache",this) + footer,
                    bccEmail, toEmail);

        } else if(now().isEqual(dateBeforeInvoiceReminder()) || now().isEqual(dateRefersToPaymentDeadline().plusDays(1))) {
            mailDetails =MailsUtility.createMailDetails(
                    "Sprawdź statusy opłacenia faktur sprzedażowych w Raks spółka NazwaSpółki",
                    executeAndCompileMustacheTemplate("template/outstandingDebtsReminderForClient.mustache",this) + footer,
                    bccEmail, toEmail);
        }
            sendEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
            log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
        } catch (Exception e) {
            log4J2PropertiesConf.performSendingInv(mailDetails.getMailTitle(), e);
        }
    }

        private LocalDate dateBeforeInvoiceReminder(){
            return LocalDate.of(now().getYear(), now().getMonth(),scheduleConfiguration.getPaymentDayInInvoiceReminder()).minusDays(1);
        }

        private LocalDate dateRefersToPaymentDeadline(){
            return LocalDate.of(now().getYear(),now().getMonth(),scheduleConfiguration.getPaymentDate());
        }

    }
