package pl.szmaus.mssql.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.repository.OtherRemainingFileRepository;
import pl.szmaus.firebirdf00154.service.UseOtherRemainingFile;
import pl.szmaus.firebirdf00154.service.GetSalesInvoice;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.repository.ReceivedDocumentFromClientRepository;
import pl.szmaus.utility.MailsUtility;
import java.util.List;

import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class DeliveryOfDocumentsSchedulerService extends AbstractMailDetails {

    private static final Integer RECEIVED_DOCUMENTS = 201;
    private final GetSalesInvoice getSalesInvoice;
    private final ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository;
    private final UseOtherRemainingFile useOtherRemainingFile;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final ReceiveDocumentFromClient receiveDocumentFromClient;

    public DeliveryOfDocumentsSchedulerService(
        GetSalesInvoice getSalesInvoice,
        ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository,
        UseOtherRemainingFile useOtherRemainingFile,
        OtherRemainingFileRepository otherRemainingFileRepository,
        ReceiveDocumentFromClient receiveDocumentFromClient,
        ScheduleConfiguration scheduleConfiguration,
        SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.getSalesInvoice = getSalesInvoice;
        this.receivedDocumentFromClientRepository = receivedDocumentFromClientRepository;
        this.useOtherRemainingFile = useOtherRemainingFile;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.receiveDocumentFromClient = receiveDocumentFromClient;
    }

    @Scheduled(cron = "${scheduling.cronDeliveryOfDocuments}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            getSalesInvoice.issuedInvoicesList( now().getMonth(), now().getYear())
                    .stream()
                    .forEach(d -> {
                        String toEmail = "";
                        String bccEmail = "";
                        List<Company> companyList = getCompany.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByNumberAndIdTypeOtherFile(companyList.get(0).getRaksNumber().toString(), RECEIVED_DOCUMENTS);
                        ReceivedDocumentFromClient receivedDocumentFromClient = receivedDocumentFromClientRepository.findByIdCompany(companyList.get(0).getId());
                        if (getCompany.ifLackOfInformationInCompany(d.getTaxIdReceiver())) {
                            mailDetails = getCompany.checkEmailAndTaxId(d.getTaxIdReceiver(), d.getFullNameReceiver());
                        } else if (useOtherRemainingFile.ifReceivedDocument(otherRemainingFile, companyList.get(0).getId())) {
                            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailIt() : mailConfiguration.getToEmail();
                            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailIt() : mailConfiguration.getBccEmail();
                            mailDetails = MailsUtility.createMailDetails(
                                    "Dokumenty firmy: " + companyList.get(0).getShortName() + " zostały dostarczone",
                                    executeAndCompileMustacheTemplate("template/documentsReceived.mustache", d) + footer,
                                    bccEmail, toEmail);
                        } else if (receiveDocumentFromClient.ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient)) {
                            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? companyList.get(0).getFirmEmailAddress() : mailConfiguration.getToEmail();
                            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailDocClient() :  mailConfiguration.getBccEmail();
                            mailDetails = MailsUtility.createMailDetails(
                                    " Informacja o terminie przekazania dokumentów księgowych dla firmy " + companyList.get(0).getShortName(),
                                    executeAndCompileMustacheTemplate("template/documentsReminder1.mustache", d) + footer,
                                    bccEmail, toEmail);
                        } else if (receiveDocumentFromClient.ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)) {
                            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? companyList.get(0).getFirmEmailAddress() : mailConfiguration.getToEmail();
                            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailDocClient() :  mailConfiguration.getBccEmail();
                            mailDetails = MailsUtility.createMailDetails(
                                    "Minął termin przekazywania dokumentów księgowych firmy: " + companyList.get(0).getShortName(),
                                    executeAndCompileMustacheTemplate("template/documentsReminder2.mustache", d) + footer,
                                    bccEmail, toEmail);
                        } else if (receiveDocumentFromClient.ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient)) {
                            toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getToEmailDocClient() : mailConfiguration.getToEmail();
                            bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailClient() :  mailConfiguration.getBccEmail();
                            mailDetails =MailsUtility.createMailDetails(
                                    "Do firmy " + companyList.get(0).getShortName() + " wysłano już dwa przypomnienia dotyczące dostarczenia dokumentów",
                                    executeAndCompileMustacheTemplate("template/documentsReminder3.mustache", d) + footer,
                                    bccEmail, toEmail);
                        }
                        if (ifEmailShouldBeSent(d, otherRemainingFile, receivedDocumentFromClient)) {
                            sendEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(),mailDetails.getBccEmail(),mailDetails.getMailBody(),mailDetails.getMailTitle());
                            log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                            receiveDocumentFromClient.checkStatusForDocuments(receivedDocumentFromClient, companyList, otherRemainingFile);
                        }
                    });
        } catch (Exception e) {
            log4J2PropertiesConf.performSendingInv(mailDetails.getMailTitle(), e);
        }
    }

    private Boolean ifEmailShouldBeSent(SalesInvoice salesInvoice, OtherRemainingFile otherRemainingFile, ReceivedDocumentFromClient receivedDocumentFromClient) {
        List<Company> companyList = getCompany.findListCompanyFindByTaxId(salesInvoice.getTaxIdReceiver());
        return  getCompany.ifLackOfInformationInCompany(salesInvoice.getTaxIdReceiver()) ||
                useOtherRemainingFile.ifReceivedDocument(otherRemainingFile, companyList.get(0).getId()) ||
                receiveDocumentFromClient.ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient) ||
                receiveDocumentFromClient.ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)  ||
                receiveDocumentFromClient.ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient);
    }
}
