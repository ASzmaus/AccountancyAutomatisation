package pl.szmaus.firebirdraks3000.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.entity.OtherRemainingFile;
import pl.szmaus.firebirdf00154.repository.OtherRemainingFileRepository;
import pl.szmaus.firebirdf00154.service.OtherRemainingFileService;
import pl.szmaus.firebirdf00154.service.SendingEmailMicrosoft;
import pl.szmaus.firebirdraks3000.command.R3ReturnCommand;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.mapper.R3ReturnMapper;
import pl.szmaus.firebirdraks3000.repository.R3ReturnRepository;
import pl.szmaus.utility.MailsUtility;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.LocalDate.now;
@Log4j2
@Service
public class R3ReturnSchedulerService extends AbstractMailDetails {

    private static final Integer CURRENT_RETURN_MONTH = 1;
    private static final String PODATEK_LINIOWY ="podatek liniowy";
    private static final String SKALA_PODATKOWA ="skala podatkowa";
    private static final String ZRYCZALTOWANY_PODATEK ="zryczaltowany podatek";
    private static final int JPK_V7K_NO =702;
    private static final int JPK_V7M_NO =703;
    private static final int CIT8_NO = 63;
    private static final int RYCZALTM_NO = 735;
    private static final int RYCZALTK_NO = 734;
    private static final int PIT5L_NO = 730;
    private static final int PIT5_NO = 733;
    private final R3ReturnRepository r3ReturnRepository;
    private final R3ReturnService r3ReturnService;
    private final OtherRemainingFileService otherRemainingFileService;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final R3ReturnMapper r3ReturnMapper;

    public R3ReturnSchedulerService(R3ReturnRepository r3ReturnRepository, R3ReturnService r3ReturnService, OtherRemainingFileService otherRemainingFileService, OtherRemainingFileRepository otherRemainingFileRepository, R3ReturnMapper r3ReturnMapper, ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        super(scheduleConfiguration,  sendingEmailMicrosoft, mailConfiguration, companyService);
        this.r3ReturnRepository = r3ReturnRepository;
        this.r3ReturnService = r3ReturnService;
        this.otherRemainingFileService = otherRemainingFileService;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.r3ReturnMapper = r3ReturnMapper;
    }

    @Override
    @Scheduled(cron = "${scheduling.cronReturns}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            r3ReturnService.monthR3ReturnList(now().getMonth().minus(CURRENT_RETURN_MONTH), now().getYear())
                .stream()
                .filter(p->  p.getEmailDataSent()==null && (ifVat(p) ||  ifCit(p) ||ifPit(p) || ifRyczalt(p)))
                .forEach(d -> {
                    String toEmail = "";
                    String bccEmail = "";
                    if (super.getMailConfiguration().getBlockToEmailProd().equals(false)) {
                       toEmail = super.getMailConfiguration().getToEmailTax();
                       bccEmail = super.getMailConfiguration().getBccEmailTax();
                    } else if (super.getMailConfiguration().getBlockToEmailProd().equals(true)) {
                        toEmail = super.getMailConfiguration().getToEmail();
                        bccEmail = super.getMailConfiguration().getBccEmail();
                    }
                    String nameOfTaxReturn= switchTaxReturnNo(d.getId_definition_return());
                    Boolean tempStatus = null;
                    if(d.getNip()==null){
                        tempStatus=true;
                        super.setMailDetails(MailsUtility.createMailDetails(
                                d.getNameOwner()+ " nie ma NIP-u na deklaracji " + nameOfTaxReturn + " w Raks",
                                super.executeAndCompileMustacheTemplate("template/noTaxId.mustache",d) + super.getFooter(),
                                bccEmail, toEmail, null,null));
                    }else {
                        List<Company> companyList =super.getCompanyService().findListCompanyFindByTaxId(d.getNip());
                        if (super.getCompanyService().ifLackOfInformationInCompany(d.getNip())) {
                            tempStatus=true;
                            super.setMailDetails(super.getCompanyService().checkEmailAndTaxId(companyList, super.getEmailAttachment(), d.getNip().replaceAll("\\D", ""), d.getNameOwner(), super.getImagesMap(),toEmail,bccEmail));
                        } else if (ifVat(d)) {
                            tempStatus = false;
                            d.setEmailSent(tempStatus);
                            String body = "";
                            if (checkIfVatReturnShouldBeSend(d)) {
                                R3ReturnCommand r3ReturnCommand = r3ReturnMapper.mapR3ReturnToR3ReturnCommand(d);
                                String messageTitle = "VAT firmy " + companyList.get(0).getShortName() + " za " + d.getReturnDate().toString().substring(5, 7) + "/" + d.getReturnDate().getYear();
                                if (d.getTax() != 0) {
                                    body = super.executeAndCompileMustacheTemplate("template/vatReturn.mustache", r3ReturnCommand) + super.getFooter();
                                } else {
                                    body = super.executeAndCompileMustacheTemplate("template/vatReturnNoTax.mustache", r3ReturnCommand) + super.getFooter();
                                }
                                super.setMailDetails(MailsUtility.createMailDetails(
                                        messageTitle,
                                        body,
                                        bccEmail, toEmail, null, null));
                            }
                        }  else if (ifCit(d)) {
                            tempStatus=false;
                            d.setEmailSent(tempStatus);
                            OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByTaxIdAndName("CIT",d.getNip());
                            if(otherRemainingFile==null)
                                otherRemainingFileService.createAdditionalRecordForOtherRemainingFile(d.getNip(), "","PLN", d.getNameOwner(), "CIT");
                           if(checkIfReturnShouldBeSend(d.getNip(), "CIT")  ){
                                R3ReturnCommand r3ReturnCommand= r3ReturnMapper.mapR3ReturnToR3ReturnCommand(d);
                                String body = "";
                                String messageTitle = "CIT firmy " + companyList.get(0).getShortName() + " za " + d.getReturnDate().toString().substring(5, 7) + "/" + d.getReturnDate().getYear();
                               if (d.getTax() != 0) {
                                   body = super.executeAndCompileMustacheTemplate("template/citReturn.mustache",r3ReturnCommand) + super.getFooter();
                               } else {
                                   body = super.executeAndCompileMustacheTemplate("template/citReturnNoTax.mustache",r3ReturnCommand) + super.getFooter();
                               }
                               super.setMailDetails(MailsUtility.createMailDetails(
                                        messageTitle,
                                         body,
                                        bccEmail, toEmail, null, null));
                            }
                        } else if (ifPit(d)) {
                            tempStatus=false;
                            d.setEmailSent(tempStatus);
                            OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByTaxIdAndName("PIT",d.getNip());
                            if(otherRemainingFile==null)
                                otherRemainingFileService.createAdditionalRecordForOtherRemainingFile(d.getNip(), "","PLN", d.getNameOwner(), "PIT");
                            if(checkIfReturnShouldBeSend(d.getNip(), "PIT")  ){
                                R3ReturnCommand r3ReturnCommand= r3ReturnMapper.mapR3ReturnToR3ReturnCommand(d);
                                String body = "";
                                String messageTitle = "PIT firmy " + companyList.get(0).getShortName() + " za " + d.getReturnDate().toString().substring(5, 7) + "/" + d.getReturnDate().getYear();
                                if (d.getTax() != 0) {
                                    body = super.executeAndCompileMustacheTemplate("template/pitReturn.mustache",r3ReturnCommand) + super.getFooter();
                                } else {
                                    body = super.executeAndCompileMustacheTemplate("template/pitReturnNoTax.mustache",r3ReturnCommand) + super.getFooter();
                                }
                                super.setMailDetails(MailsUtility.createMailDetails(
                                        messageTitle,
                                         body,
                                        bccEmail, toEmail, null, null));
                            }
                        } else if (ifRyczalt(d)) {
                            tempStatus=false;
                            d.setEmailSent(false);
                            OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByTaxIdAndName("RYCZALT",d.getNip());
                            if(otherRemainingFile==null)
                                otherRemainingFileService.createAdditionalRecordForOtherRemainingFile(d.getNip(), "","PLN", d.getNameOwner(), "RYCZALT");
                            if(checkIfReturnShouldBeSend(d.getNip(), "RYCZALT")  ){
                                R3ReturnCommand r3ReturnCommand= r3ReturnMapper.mapR3ReturnToR3ReturnCommand(d);
                                String body = "";
                                String messageTitle = "Ryczałt ewidencjonowany firmy " + companyList.get(0).getShortName() + " za " + d.getReturnDate().toString().substring(5, 7) + "/" + d.getReturnDate().getYear();
                                if (d.getTax() != 0) {
                                    body = super.executeAndCompileMustacheTemplate("template/ryczaltReturn.mustache",r3ReturnCommand) + super.getFooter();
                                } else {
                                    body = super.executeAndCompileMustacheTemplate("template/ryczaltReturnNoTax.mustache",r3ReturnCommand) + super.getFooter();
                                }
                                super.setMailDetails(MailsUtility.createMailDetails(
                                        messageTitle,
                                        body,
                                        bccEmail, toEmail, null, null));
                            }
                        }
                    }

                   if(ifEmailShouldBeSent(d) ){
                      super.getSendingEmailMicrosoft().configurationMicrosoft365Email(super.getMailDetails().getToEmail(),super.getMailDetails().getBccEmail(), super.getMailDetails().getMailBody(), super.getMailDetails().getMailTitle(), super.getMailDetails().getAttachmentInvoice(), super.getMailDetails().getImagesMap());
                      log4J2PropertiesConf.performSomeTask(super.getMailDetails().getToEmail(), super.getMailDetails().getBccEmail(), super.getMailDetails().getMailTitle(), super.getMailDetails().getMailBody());
                      d.setEmailSent(tempStatus);
                      if(ifStatusShouldBeSaved(d)){
                                DateTimeFormatter sendingEmaliFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                d.setEmailDataSent(LocalDate.parse(now().format(sendingEmaliFormater)));
                                r3ReturnRepository.save(d);
                      }
                   }
                });
        } catch (Exception e) {
            log.error(e);
        }
    }
    public Boolean ifVat(R3Return r3Return){
        return r3Return.getId_definition_return() == JPK_V7M_NO || r3Return.getId_definition_return() == JPK_V7K_NO;
    }
    private Boolean ifPit(R3Return r3Return){
        return r3Return.getId_definition_return() == PIT5L_NO || r3Return.getId_definition_return() == PIT5_NO;
    }

    private Boolean ifCit(R3Return r3Return){
        return r3Return.getId_definition_return() == CIT8_NO;
    }

    private Boolean ifRyczalt(R3Return r3Return){
        return r3Return.getId_definition_return() == RYCZALTM_NO || r3Return.getId_definition_return() == RYCZALTK_NO;
    }

    private String  switchTaxReturnNo(int taxNumber){
        String nameOfTaxRetur ="";
        switch (taxNumber) {
            case PIT5L_NO:
                nameOfTaxRetur = PODATEK_LINIOWY;
                break;
            case PIT5_NO:
                nameOfTaxRetur = SKALA_PODATKOWA;
                break;
            case RYCZALTK_NO:
                nameOfTaxRetur = ZRYCZALTOWANY_PODATEK;
                break;
            case RYCZALTM_NO:
                nameOfTaxRetur = ZRYCZALTOWANY_PODATEK;
                break;
            default:
                nameOfTaxRetur = "unknown RETURN";
                break;
        }
        return nameOfTaxRetur;
    }
    private Boolean ifStatusShouldBeSaved(R3Return r3Return) {
        return (ifVat(r3Return) && checkIfVatReturnShouldBeSend(r3Return) ||  checkIfReturnShouldBeSend(r3Return.getNip(),"CIT") || checkIfReturnShouldBeSend(r3Return.getNip(), "PIT") || checkIfReturnShouldBeSend(r3Return.getNip(), "RYCZALT")) && r3Return.getEmailSent()==false ;
    }

    private Boolean ifEmailShouldBeSent(R3Return r3Return) {
        if (r3Return.getNip() == null) {
            return r3Return.getEmailSent() == false && getMailDetails() != null;
        } else if (r3Return.getNip() != null) {
            return r3Return.getEmailSent() == false && getMailDetails() != null && (super.getCompanyService().ifLackOfInformationInCompany(r3Return.getNip())) || ifStatusShouldBeSaved(r3Return);
        }
        return false;
    }

    private Boolean checkIfReturnShouldBeSend(String taxId, String nameReturn){
        OtherRemainingFile otherRemainingFile = otherRemainingFileRepository.findByTaxIdAndName(nameReturn,taxId);
        return otherRemainingFile!=null
                && otherRemainingFile.getName().length()>=7
                && otherRemainingFile.getName().substring(0,7).equals(now().minusMonths(CURRENT_RETURN_MONTH).toString().substring(0,7));
    }

    private Boolean checkIfVatReturnShouldBeSend(R3Return r3Return){
        return r3Return.getEReturnStatusProcess() == 4 || r3Return.getEReturnStatusProcess() == 2;
    }
}
