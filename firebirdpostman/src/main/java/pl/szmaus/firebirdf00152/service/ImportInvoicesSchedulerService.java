package pl.szmaus.firebirdf00152.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.utility.DateUtility;
import pl.szmaus.utility.MailsUtility;

import static java.time.LocalDate.now;
@Log4j2
@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class ImportInvoicesSchedulerService extends AbstractMailDetails {

    private final DateUtility dateUtility;
    private final BookingSalesInvoiceService bookingSalesInvoiceService;

    public ImportInvoicesSchedulerService(ScheduleConfiguration scheduleConfiguration, SendEmailMicrosoft sendEmailMicrosoft, MailConfiguration mailConfiguration, GetCompany getCompany, DateUtility dateUtility, BookingSalesInvoiceService bookingSalesInvoiceService) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.dateUtility = dateUtility;
        this.bookingSalesInvoiceService = bookingSalesInvoiceService;
    }

    @Scheduled(cron = "${scheduling.cronImportDocs}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try {
            bookingSalesInvoiceService.bookingSalesInvoiceInRaks();
            String toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailTax() : mailConfiguration.getToEmail();
            String bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailTax() : mailConfiguration.getBccEmail();
            mailDetails = MailsUtility.createMailDetails(
                    "Zaksięgowano faktury w kwiatekchol za miesiąc" + dateUtility.extractPreviousMonthAndYear(),
                    "Zaksięgowano faktury w kwiatekchol za miesiąc" + dateUtility.extractPreviousMonthAndYear() + footer,
                    bccEmail, toEmail);
            sendEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
            log4J2PropertiesConf.logSentMail(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
        } catch (Exception e) {
           log.error(e);
        }
    }
}
