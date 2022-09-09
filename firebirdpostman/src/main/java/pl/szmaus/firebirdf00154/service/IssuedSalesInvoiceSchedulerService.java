package pl.szmaus.firebirdf00154.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.Enum.InvoiceStatus;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.command.SalesInvoiceCommand;
import pl.szmaus.firebirdf00154.mapper.SalesInvoiceMapper;
import pl.szmaus.firebirdf00154attachment.entity.R3DocumentFiles;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.firebirdf00154attachment.repository.R3DocumentFilesRepository;
import pl.szmaus.utility.MailsUtility;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static java.time.LocalDate.now;

@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class IssuedSalesInvoiceSchedulerService extends AbstractMailDetails {
    private static final String IMAGES_QR_ID = "bufferideaId";
    private static final String TEMPLATE_QR_INVOICE_NAME = "000000000";
    private final SalesInvoiceService salesInvoiceService;
    private final R3DocumentFilesRepository r3DocumentFilesRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    public IssuedSalesInvoiceSchedulerService(SalesInvoiceService salesInvoiceService, R3DocumentFilesRepository r3DocumentFilesRepository, SalesInvoiceMapper salesInvoiceMapper, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceService = salesInvoiceService;
        this.r3DocumentFilesRepository = r3DocumentFilesRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
    }

    @Scheduled(cron = "${scheduling.cronIssuedInvoice}")
    public void trackSendEmail() {
        salesInvoiceService.issuedInvoicesList(now().getMonth(),now().getYear())
                    .stream()
                    .filter(p -> ifInvoiceHasAttachment(p))
                    .forEach(d -> {
                        String tempStatus = d.getStatus();
                        String toEmail="";
                        List<Company> companyList = companyService.findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        if (companyService.ifLackOfInformationInCompany(d.getTaxIdReceiver())) {
                            mailDetails = companyService.checkEmailAndTaxId( d.getTaxIdReceiver(), d.getFullNameReceiver());
                        } else if (ifGeneratedPdfNotSend(d)) {
                            if (mailConfiguration.getBlockToEmailProd().equals(false)) { //prod
                                toEmail = companyList.get(0).getFirmEmailAddress();
                            } else if (mailConfiguration.getBlockToEmailProd().equals(true)) { // dev
                                toEmail = mailConfiguration.getToEmail();
                            }
                            SalesInvoiceCommand salesInvoiceCommand= salesInvoiceMapper.mapSalesInvoiceToSalesInvoiceCommand(d);
                            mailDetails = MailsUtility.createMailDetails("NazwaSpółki e-faktura nr " + d.getNumber(),
                                    executeAndCompileMustacheTemplate("template/invoice.mustache", salesInvoiceCommand) + footer,
                                            mailConfiguration.getBccEmail(), toEmail, emailAttachment, imagesMap);
                            salesInvoiceService.setStatusInvoice(d, InvoiceStatus.START_SENDING_INV.label);
                        }

                        if (ifEmailShouldBeSent(d)) {
                            Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
                            try {
                                imagesMap.clear();
                                BufferedImage bufferedImage = createQRCode(d,companyList);
                                byte[] bufferedImageByte = toByteArray( bufferedImage,"jpg");
                                mailDetails.getImagesMap().put( IMAGES_QR_ID + d.getNumber(), bufferedImageByte);
                                sendingEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(),mailDetails.getBccEmail(),mailDetails.getMailBody(),mailDetails.getMailTitle(),mailDetails.getAttachmentInvoice(),mailDetails.getImagesMap());
                                log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(),mailDetails.getBccEmail(),mailDetails.getMailTitle(),mailDetails.getMailBody());
                                saveStatusAfterSentInvoice(tempStatus, d, companyList);
                            } catch (Exception e) {
                                log4J2PropertiesConf.performSendingInv(mailDetails.getMailTitle(), e);
                            }
                        }
                    });
        }

     private Boolean ifInvoiceHasAttachment(SalesInvoice salesInvoice){
         R3DocumentFiles r3DocumentFiles= r3DocumentFilesRepository.findByGuid(salesInvoice.getGuid());
         if(r3DocumentFiles!=null){
             emailAttachment = r3DocumentFiles.getData();
         } else {
             emailAttachment = null;
         }
         return emailAttachment!=null;
     }


    private void saveStatusAfterSentInvoice(String tempStatus, SalesInvoice salesInvoice, List<Company> companyList) {
        if(companyList.get(0) == null || companyList.size()>1) {
            salesInvoiceService.setStatusInvoice(salesInvoice, InvoiceStatus.CHECK_NIP.label);
        }else if(!companyService.ifEmailAddressExists(companyList.get(0))){
            salesInvoiceService.setStatusInvoice(salesInvoice, InvoiceStatus.NO_EMAIL.label);
        }else if (tempStatus!=null && tempStatus.equals(InvoiceStatus.PAID_TO_SEND.label)) {
            salesInvoiceService.setStatusInvoice(salesInvoice, InvoiceStatus.PAID.label);
        } else{
            salesInvoiceService.setStatusInvoice(salesInvoice, InvoiceStatus.SENDING_INVOICE.label);
        }
    }

    private Boolean ifGeneratedPdfNotSend(SalesInvoice salesInvoice) {
        return salesInvoice.getStatus()==null || salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_INV.label) ||  salesInvoice.getStatus().equals(InvoiceStatus.PAID_TO_SEND.label);
    }

    private boolean ifEmailShouldBeSent(SalesInvoice salesInvoice) {
        List<Company> companyList =companyService.findListCompanyFindByTaxId(salesInvoice.getTaxIdReceiver());
        return companyList.get(0) == null || companyList.size()>1 || !companyService.ifEmailAddressExists(companyList.get(0)) || ifGeneratedPdfNotSend(salesInvoice) ;
    }

    private BufferedImage createQRCode(SalesInvoice salesInvoice, List<Company> companyList) throws Exception {
        String amount = salesInvoice.getGrossAmountInPln().toString().replace(".","");
        int sizeAmount = amount.length();
        String newAmount = amount.substring(0,sizeAmount-2);
        String amountInInvoice = TEMPLATE_QR_INVOICE_NAME.substring(0,TEMPLATE_QR_INVOICE_NAME.length()-newAmount.length()) + newAmount;
        return QRCodeService.generateQRCodeImage("|PL|"+ mailConfiguration.getOfficeBankAccount() + "|"+ amountInInvoice + "|" + mailConfiguration.getAccountingOffice() + "|FV " + salesInvoice.getNumber()+"|" + companyList.get(0).getRaksNumber()+"||PLN");
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        return baos.toByteArray();
    }
}
