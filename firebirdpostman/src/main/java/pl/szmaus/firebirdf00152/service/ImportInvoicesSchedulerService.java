package pl.szmaus.firebirdf00152.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.API.MicrosoftGraphAPI;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.ImportConfiguration;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.utility.DateUtility;
import pl.szmaus.utility.MailsUtility;
import com.microsoft.graph.http.GraphServiceException;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class ImportInvoicesSchedulerService extends AbstractMailDetails {

    private final DateUtility dateUtility;
    private final BookingSalesInvoiceService bookingSalesInvoiceService;
    private final MicrosoftGraphAPI microsoftGraphAPI;
    private final ImportConfiguration importConfiguration;

    public ImportInvoicesSchedulerService(ScheduleConfiguration scheduleConfiguration, SendEmailMicrosoft sendEmailMicrosoft, MailConfiguration mailConfiguration, GetCompany getCompany, DateUtility dateUtility, BookingSalesInvoiceService bookingSalesInvoiceService, MicrosoftGraphAPI microsoftGraphAPI, ImportConfiguration importConfiguration) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.dateUtility = dateUtility;
        this.bookingSalesInvoiceService = bookingSalesInvoiceService;
        this.microsoftGraphAPI = microsoftGraphAPI;
        this.importConfiguration = importConfiguration;
    }

    @Scheduled(cron = "${scheduling.cronImportDocs}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        String pathToResourceSite = importConfiguration.getPathToResourceSite();
        String completePathOfImportedFile = "Import dokumentów/KWIATEKHOL/import.csv";
        String completePathOfNewFolder;
        String toEmail;
        String bccEmail;
        try {
            completePathOfNewFolder = "Import dokumentów/KWIATEKHOL/OK";
            bookingSalesInvoiceService.bookingSalesInvoiceInRaks();
            microsoftGraphAPI.moveMyFile(completePathOfImportedFile, completePathOfNewFolder, pathToResourceSite);
            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailTax() : mailConfiguration.getToEmail();
            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailTax() : mailConfiguration.getBccEmail();
            mailDetails = MailsUtility.createMailDetails(
                    "Zaksięgowano faktury w kwiatekhol za miesiąc " + dateUtility.extractPreviousMonthAndYear(),
                    "Zaksięgowano faktury w kwiatekhol za miesiąc " + dateUtility.extractPreviousMonthAndYear() + ". Proszę sprawdź czy ksiegowania się zgadzają."+ footer,
                    bccEmail, toEmail);
            sendEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
            log4J2PropertiesConf.logSentMail(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
        } catch (GraphServiceException se) {

        } catch (Exception e) {
            completePathOfNewFolder = "Import dokumentów/KWIATEKHOL/ERROR";
            microsoftGraphAPI.moveMyFile(completePathOfImportedFile, completePathOfNewFolder, pathToResourceSite);
            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailIt() : mailConfiguration.getToEmail();
            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailIt() : mailConfiguration.getBccEmail();
            mailDetails = MailsUtility.createMailDetails(
                    "Exception w trakcie księgowania faktur w kwiatekhol za miesiąc " + dateUtility.extractPreviousMonthAndYear(),
                    "Exception w trakcie księgowania faktur w kwiatekhol za miesiąc "+ dateUtility.extractPreviousMonthAndYear() + "."+ footer,
                    bccEmail, toEmail);
            log4J2PropertiesConf.logImportError(e);
        }
    }
}
