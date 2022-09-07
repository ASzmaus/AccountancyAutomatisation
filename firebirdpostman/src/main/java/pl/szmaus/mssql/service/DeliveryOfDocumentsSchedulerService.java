package pl.szmaus.mssql.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.repository.OtherRemainingFileRepository;
import pl.szmaus.firebirdf00154.service.SendingEmailMicrosoft;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.firebirdf00154.service.OtherRemainingFileService;
import pl.szmaus.firebirdf00154.service.SalesInvoiceService;
import pl.szmaus.mssql.entity.ReceivedDocumentFromClient;
import pl.szmaus.mssql.repository.ReceivedDocumentFromClientRepository;
import pl.szmaus.utility.MailsUtility;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class DeliveryOfDocumentsSchedulerService extends AbstractMailDetails {

    private static final Integer RECEIVED_DOCUMENTS = 201;
    private static final Integer FIRST_INFO_STATUS_ID = 1;
    private static final Integer FIRST_REMINDER_STATUS_ID = 2;
    private static final Integer RECEIVED_DOCUMENT_STATUS_ID = 3;
    private static final Integer SECOND_REMINDER_STATUS_ID = 4;
    private static final Integer NO_EMAIL_ID = 5;
    private static final Integer WRONG_NIP_ID = 6;

    private final SalesInvoiceService salesInvoiceService;
    private final ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository;
    private final OtherRemainingFileService otherRemainingFileService;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final ReceivedDocumentFromClientService receivedDocumentFromClientService;

    public DeliveryOfDocumentsSchedulerService(SalesInvoiceService salesInvoiceService, ReceivedDocumentFromClientRepository receivedDocumentFromClientRepository, OtherRemainingFileService otherRemainingFileService, OtherRemainingFileRepository otherRemainingFileRepository, ReceivedDocumentFromClientService receivedDocumentFromClientService, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceService = salesInvoiceService;
        this.receivedDocumentFromClientRepository = receivedDocumentFromClientRepository;
        this.otherRemainingFileService = otherRemainingFileService;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.receivedDocumentFromClientService = receivedDocumentFromClientService;
    }

    @Transactional
    @Override
    @Scheduled(cron = "${scheduling.cronDeliveryOfDocuments}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
        if(now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getReminder2Documents()).minusDays(1))){
            String toEmail = "";
            String bccEmail = "";
            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                toEmail = super.getMailConfiguration().getToEmailDocClient();
                bccEmail = super.getMailConfiguration().getBccEmailClient();
            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                toEmail = super.getMailConfiguration().getToEmail();
                bccEmail = super.getMailConfiguration().getBccEmail();
            }
            super.setMailDetails(MailsUtility.createMailDetails(
                    "Sprawdź statusy dokumentów w kartotekach dodatkowych w Raks spółka NazwaSpółki",
                    super.executeAndCompileMustacheTemplate("template/documentsReminderForClient.mustache",this) + super.getFooter(),
                    bccEmail, toEmail, null, null));

            super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
            log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
        }else {
            salesInvoiceService.issuedInvoicesList( now().getMonth(), now().getYear())
                    .stream()
                    .forEach(d -> {
                        String toEmail = "";
                        String bccEmail = "";
                        List<Company> companyList = super.getCompanyService().findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByNumberAndIdTypeOtherFile(companyList.get(0).getRaksNumber().toString(), RECEIVED_DOCUMENTS);
                        ReceivedDocumentFromClient receivedDocumentFromClient = receivedDocumentFromClientRepository.findByIdCompany(companyList.get(0).getId());
                        if (companyList.get(0) == null || companyList.size() > 1 || !super.getCompanyService().ifEmailAddressExists(companyList.get(0))) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = super.getMailConfiguration().getToEmailClient();
                                bccEmail = super.getMailConfiguration().getBccEmailClient();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(super.getCompanyService().checkEmailAndTaxId(companyList, null, d.getTaxIdReceiver(), d.getFullNameReceiver(), null, toEmail, bccEmail));
                        } else if (ifReceivedDocument(otherRemainingFile, companyList.get(0).getId())) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = super.getMailConfiguration().getToEmailIt();
                                bccEmail = super.getMailConfiguration().getBccEmailIt();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    "Dokumenty firmy: " + companyList.get(0).getShortName() + " zostały dostarczone",
                                    super.executeAndCompileMustacheTemplate("template/documentsReceived.mustache",d) + super.getFooter(),
                                    bccEmail, toEmail, super.getEmailAttachment(), super.getImagesMap()));
                        } else if (ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient)) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = companyList.get(0).getFirmEmailAddress();
                                bccEmail = super.getMailConfiguration().getBccEmailDocClient();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    " Informacja o terminie przekazania dokumentów księgowych dla firmy " + companyList.get(0).getShortName(),
                                    super.executeAndCompileMustacheTemplate("template/documentsReminder1.mustache",d) + super.getFooter(),
                                    bccEmail, toEmail, null, null));
                        } else if (ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = companyList.get(0).getFirmEmailAddress();
                                bccEmail = super.getMailConfiguration().getBccEmailDocClient();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    "Minął termin przekazywania dokumentów księgowych firmy: " + companyList.get(0).getShortName(),
                                    super.executeAndCompileMustacheTemplate("template/documentsReminder2.mustache",d) + super.getFooter(),
                                    bccEmail, toEmail, null, null));
                        } else if (ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient)) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = super.getMailConfiguration().getToEmailDocClient();
                                bccEmail = super.getMailConfiguration().getBccEmailClient();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(MailsUtility.createMailDetails(
                                    "Do firmy " + companyList.get(0).getShortName() + " wysłano już dwa przypomnienia dotyczące dostarczenia dokumentów",
                                    super.executeAndCompileMustacheTemplate("template/documentsReminder3.mustache",d) + super.getFooter(),
                                    bccEmail, toEmail, null, null));
                        }
                        if (ifEmailShouldBeSent(d, otherRemainingFile, receivedDocumentFromClient)) {
                            super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
                            log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
                            if (companyList.get(0) == null || companyList.size() > 1) {
                                receivedDocumentFromClientService.editReceivedDocumentFromClient(receivedDocumentFromClient, WRONG_NIP_ID);
                            } else if (!super.getCompanyService().ifEmailAddressExists(companyList.get(0))) {
                                receivedDocumentFromClientService.editReceivedDocumentFromClient(receivedDocumentFromClient, NO_EMAIL_ID);
                            } else if (ifReceivedDocument(otherRemainingFile, companyList.get(0).getId())) {
                                saveStatusForDocuments(receivedDocumentFromClient, companyList.get(0).getId(), RECEIVED_DOCUMENT_STATUS_ID);
                            } else if (ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient)) {
                                saveStatusForDocuments(receivedDocumentFromClient, companyList.get(0).getId(), FIRST_INFO_STATUS_ID);
                            } else if (ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)) {
                                receivedDocumentFromClientService.editReceivedDocumentFromClient(receivedDocumentFromClient, FIRST_REMINDER_STATUS_ID);
                            } else if (ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient)) {
                                receivedDocumentFromClientService.editReceivedDocumentFromClient(receivedDocumentFromClient, SECOND_REMINDER_STATUS_ID);
                            }
                        }
                    });
        }
        } catch (Exception e) {
            log4J2PropertiesConf.performSendingInv(super.getMailDetails().getMailTitle(), e);
        }
    }
    private Boolean ifReceivedDocument(OtherRemainingFile otherRemainingFile, Integer idCompany) {
        return otherRemainingFile != null && otherRemainingFileService.checkIfRecivedDocumentFromFirebird(idCompany) && (now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getReminder1Documents())) || now().isEqual(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getReminder2Documents())) || now().isAfter(LocalDate.of(now().getYear(),now().getMonth(),super.getScheduleConfiguration().getReminder3Documents())));
    }
    private Boolean ifNotReceivedDocumentFirstInfo(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return (receivedDocumentFromClient == null || !receivedDocumentFromClient.getData().equals(now().minusMonths(1).toString().substring(0, 7)) )
                && now().isBefore(LocalDate.of(now().getYear(),now().getMonth(), super.getScheduleConfiguration().getReminder2Documents()));
    }
    private Boolean ifNotReceivedDocumentFirstReminder(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return receivedDocumentFromClient.getIdReceivedDocumentFromClientStatus() == FIRST_INFO_STATUS_ID && receivedDocumentFromClient.getData().equals(now().minusMonths(1).toString().substring(0, 7))
                && now().isAfter(LocalDate.of(now().getYear(),now().getMonth(), super.getScheduleConfiguration().getReminder2Documents()).minusDays(1))
                && now().isBefore(LocalDate.of(now().getYear(),now().getMonth(), super.getScheduleConfiguration().getReminder3Documents()));
    }
    private Boolean ifNotReceivedDocumentSecondReminder(ReceivedDocumentFromClient receivedDocumentFromClient) {
        return receivedDocumentFromClient.getIdReceivedDocumentFromClientStatus() == FIRST_REMINDER_STATUS_ID && receivedDocumentFromClient.getData().equals(now().minusMonths(1).toString().substring(0, 7))
                && now().isAfter(LocalDate.of(now().getYear(),now().getMonth(), super.getScheduleConfiguration().getReminder3Documents()));
    }
    private Boolean ifEmailShouldBeSent(SalesInvoice salesInvoice, OtherRemainingFile otherRemainingFile, ReceivedDocumentFromClient receivedDocumentFromClient) {
        List<Company> companyList = super.getCompanyService().findListCompanyFindByTaxId(salesInvoice.getTaxIdReceiver());
        return  companyList.get(0) == null || companyList.size()>1 || !super.getCompanyService().ifEmailAddressExists(companyList.get(0)) || ifReceivedDocument(otherRemainingFile, companyList.get(0).getId())
                || ifNotReceivedDocumentFirstInfo(receivedDocumentFromClient) || ifNotReceivedDocumentFirstReminder(receivedDocumentFromClient)  || ifNotReceivedDocumentSecondReminder(receivedDocumentFromClient);
    }

    private void saveStatusForDocuments(ReceivedDocumentFromClient receivedDocumentFromClient, Integer idCompany, Integer idStatusDocuments) {
        if (receivedDocumentFromClient == null) {
            ReceivedDocumentFromClient receivedDocumentsFromClients1 = new ReceivedDocumentFromClient();
            receivedDocumentFromClientService.saveReceivedDocumentFromClient(receivedDocumentsFromClients1, idCompany, idStatusDocuments);
        } else {
            receivedDocumentFromClientService.editReceivedDocumentFromClient(receivedDocumentFromClient, idStatusDocuments);
        }
    }
}
