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
import pl.szmaus.firebirdf00154.repository.SalesInvoiceRepository;
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
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesInvoiceService salesInvoiceService;
    private final R3DocumentFilesRepository r3DocumentFilesRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    public IssuedSalesInvoiceSchedulerService(SalesInvoiceRepository salesInvoiceRepository, SalesInvoiceService salesInvoiceService, R3DocumentFilesRepository r3DocumentFilesRepository, SalesInvoiceMapper salesInvoiceMapper, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
       super(scheduleConfiguration, sendingEmailMicrosoft, mailConfiguration, companyService);
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.salesInvoiceService = salesInvoiceService;
        this.r3DocumentFilesRepository = r3DocumentFilesRepository;
        this.salesInvoiceMapper = salesInvoiceMapper;
    }
    @Override
    @Scheduled(cron = "${scheduling.cronIssuedInvoice}")
    public void trackSendEmail() {
            salesInvoiceService.issuedInvoicesList(now().getMonth(),now().getYear())
                    .stream()
                    .filter(p -> {
                        R3DocumentFiles r3DocumentFiles= r3DocumentFilesRepository.findByGuid(p.getGuid());
                            if(r3DocumentFiles!=null){
                                super.setEmailAttachment(r3DocumentFiles.getData());
                            } else {
                                super.setEmailAttachment(null);
                            }
                       return super.getEmailAttachment()!=null;
                    })
                    .forEach(d -> {
                        String tempStatus = d.getStatus();
                        String toEmail="";
                        String bccEmail="";
                        List<Company> companyList = super.getCompanyService().findListCompanyFindByTaxId(d.getTaxIdReceiver());
                        if (companyList.get(0) == null || companyList.size() > 1 || !super.getCompanyService().ifEmailAddressExists(companyList.get(0))) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = super.getMailConfiguration().getToEmailClient();
                                bccEmail = super.getMailConfiguration().getBccEmailClient();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                                bccEmail = super.getMailConfiguration().getBccEmail();
                            }
                            super.setMailDetails(super.getCompanyService().checkEmailAndTaxId(companyList, super.getEmailAttachment(), d.getTaxIdReceiver(), d.getFullNameReceiver(), super.getImagesMap(),toEmail,bccEmail));
                        } else if (ifGeneratedPdfNotSend(d)) {
                            if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) { //prod
                                toEmail = companyList.get(0).getFirmEmailAddress();
                            } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) { // dev
                                toEmail = super.getMailConfiguration().getToEmail();
                            }

                            SalesInvoiceCommand salesInvoiceCommand= salesInvoiceMapper.mapSalesInvoiceToSalesInvoiceCommand(d);
                            super.setMailDetails(MailsUtility.createMailDetails("NazwaSpółki e-faktura nr " + d.getNumber(),
                                    super.executeAndCompileMustacheTemplate("template/invoice.mustache",salesInvoiceCommand) + super.getFooter(),
                                            super.getMailConfiguration().getBccEmail(), toEmail, super.getEmailAttachment(), super.getImagesMap()));
                            d.setStatus(InvoiceStatus.START_SENDING_INV.label);
                            salesInvoiceRepository.save(d);
                        }

                        if (ifEmailShouldBeSent(d)) {
                            Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
                            try {
                                super.getImagesMap().clear();
                                BufferedImage bufferedImage = createQRCode(d,companyList);
                                byte[] bufferedImageByte = toByteArray( bufferedImage,"jpg");
                                super.getMailDetails().getImagesMap().put( IMAGES_QR_ID+d.getNumber(), bufferedImageByte);
                                super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
                                log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
                                d.setStatus(InvoiceStatus.SENDING_INVOICE.label);
                                salesInvoiceRepository.save(d);
                                saveStatusForInvoices(tempStatus, d, companyList);
                            } catch (Exception e) {
                                log4J2PropertiesConf.performSendingInv(super.getMailDetails().getMailTitle(), e);
                            }
                        }
                    });
        }

    private Boolean ifGeneratedPdfNotSend(SalesInvoice salesInvoice) {
        return salesInvoice.getStatus()==null || salesInvoice.getStatus().equals(InvoiceStatus.START_SENDING_INV.label) ||  salesInvoice.getStatus().equals(InvoiceStatus.PAID_TO_SEND.label);
    }

    private boolean ifEmailShouldBeSent(SalesInvoice salesInvoice) {
        List<Company> companyList =super.getCompanyService().findListCompanyFindByTaxId(salesInvoice.getTaxIdReceiver());
        return companyList.get(0) == null || companyList.size()>1 || !super.getCompanyService().ifEmailAddressExists(companyList.get(0)) || ifGeneratedPdfNotSend(salesInvoice) ;
    }
    private void saveStatusForInvoices(String tempStatus, SalesInvoice salesInvoice, List<Company> companyList) {
         if(companyList.get(0) == null || companyList.size()>1) {
             salesInvoice.setStatus(InvoiceStatus.CHECK_NIP.label);
         }else if(!super.getCompanyService().ifEmailAddressExists(companyList.get(0))){
             salesInvoice.setStatus(InvoiceStatus.NO_EMAIL.label);
        }else if (tempStatus!=null && tempStatus.equals(InvoiceStatus.PAID_TO_SEND.label)) {
            salesInvoice.setStatus(InvoiceStatus.PAID.label);
        }
        salesInvoiceRepository.save(salesInvoice);
    }

    private BufferedImage createQRCode(SalesInvoice salesInvoice, List<Company> companyList) throws Exception {
        String amountInInvoice =TEMPLATE_QR_INVOICE_NAME;
        String amount = salesInvoice.getGrossAmountInPln().toString().replace(".","");
        int sizeAmount = amount.length();
        String newAmount = amount.substring(0,sizeAmount-2);
        amountInInvoice = TEMPLATE_QR_INVOICE_NAME.substring(0,TEMPLATE_QR_INVOICE_NAME.length()-newAmount.length()) + newAmount;
        BufferedImage bufferedImage = QRCodeService.generateQRCodeImage("|PL|"+ super.getMailConfiguration().getOfficeBankAccount() + "|"+ amountInInvoice + "|" + super.getMailConfiguration().getAccountingOffice() + "|FV " + salesInvoice.getNumber()+"|" + companyList.get(0).getRaksNumber()+"||PLN");
        return bufferedImage;
    }

    public static byte[] toByteArray(BufferedImage bi, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }
}
