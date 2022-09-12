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
import pl.szmaus.firebirdf00154.service.UseOtherRemainingFile;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.command.R3ReturnCommand;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.mapper.R3ReturnMapper;
import pl.szmaus.firebirdraks3000.repository.R3ReturnRepository;
import pl.szmaus.utility.MailDetails;
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
    private final ReturnR3Declaration returnR3Declaration;
    private final UseOtherRemainingFile useOtherRemainingFile;
    private final OtherRemainingFileRepository otherRemainingFileRepository;
    private final R3ReturnMapper r3ReturnMapper;

    public R3ReturnSchedulerService(
        R3ReturnRepository r3ReturnRepository,
        ReturnR3Declaration returnR3Declaration,
        UseOtherRemainingFile useOtherRemainingFile,
        OtherRemainingFileRepository otherRemainingFileRepository,
        R3ReturnMapper r3ReturnMapper,
        ScheduleConfiguration scheduleConfiguration,
        SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.r3ReturnRepository = r3ReturnRepository;
        this.returnR3Declaration = returnR3Declaration;
        this.useOtherRemainingFile = useOtherRemainingFile;
        this.otherRemainingFileRepository = otherRemainingFileRepository;
        this.r3ReturnMapper = r3ReturnMapper;
    }

    @Scheduled(cron = "${scheduling.cronReturns}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            returnR3Declaration.monthR3ReturnList(now().getMonth().minus(CURRENT_RETURN_MONTH), now().getYear())
                .stream()
                .filter(p->  p.getEmailDataSent()==null && (ifVat(p) ||  ifCit(p) ||ifPit(p) || ifRyczalt(p)))
                .forEach(d -> {
                    String nameOfTaxReturn= switchTaxReturnNo(d.getId_definition_return());
                    Boolean tempStatus = false;
                    if(d.getNip()==null){
                        String toEmail = "";
                        String bccEmail = "";
                        if (mailConfiguration.getBlockToEmailProd().equals(false)) {
                            toEmail = mailConfiguration.getToEmailTax();
                            bccEmail = mailConfiguration.getBccEmailTax();
                        } else if (mailConfiguration.getBlockToEmailProd().equals(true)) {
                            toEmail = mailConfiguration.getToEmail();
                            bccEmail = mailConfiguration.getBccEmail();
                        }
                        tempStatus=true;
                        mailDetails =MailsUtility.createMailDetails(
                                d.getNameOwner()+ " nie ma NIP-u na deklaracji " + nameOfTaxReturn + " w Raks",
                                executeAndCompileMustacheTemplate("template/noTaxId.mustache",d) + footer,
                                bccEmail, toEmail);
                    }else {
                        List<Company> companyList = getCompany.findListCompanyFindByTaxId(d.getNip());
                        if (getCompany.ifLackOfInformationInCompany(d.getNip())) {
                            tempStatus=true;
                            mailDetails = getCompany.checkEmailAndTaxId(d.getNip(), d.getNameOwner());
                        } else if (ifVat(d)) {
                            mailDetails = returnR3MailDetails(d, "VAT", companyList);
                        }  else if (ifCit(d)) {
                            if(useOtherRemainingFile.checkOtherRemainingFile("CIT",d)==true)
                                mailDetails = returnR3MailDetails(d, "CIT", companyList);
                        } else if (ifPit(d)) {
                            if(useOtherRemainingFile.checkOtherRemainingFile("PIT",d)==true)
                                mailDetails = returnR3MailDetails(d, "PIT", companyList);
                        } else if (ifRyczalt(d)) {
                            if(useOtherRemainingFile.checkOtherRemainingFile("RYCZALT",d)==true)
                                mailDetails = returnR3MailDetails(d, "RYCZALT", companyList);
                        }
                    }
                    if( mailDetails!=null) {
                        sendEmailMicrosoft.configurationMicrosoft365Email(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailBody(), mailDetails.getMailTitle(), mailDetails.getAttachmentInvoice(), mailDetails.getImagesMap());
                        log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                        saveSatausReturn(d, tempStatus);
                    }
                });
        } catch (Exception e) {
            log.error(e);
        }
    }

    private MailDetails returnR3MailDetails(R3Return r3Return, String returnName, List<Company> companyList){
        String toEmail = "";
        String bccEmail = "";
        if (mailConfiguration.getBlockToEmailProd().equals(false)) {
            toEmail =companyList.get(0).getFirmEmailAddress();
            bccEmail = mailConfiguration.getBccEmailTax();
        } else if (mailConfiguration.getBlockToEmailProd().equals(true)) {
            toEmail = mailConfiguration.getToEmail();
            bccEmail = mailConfiguration.getBccEmail();
        }
        if (isReturnShouldBeSend(r3Return, returnName)) {
            R3ReturnCommand r3ReturnCommand = r3ReturnMapper.mapR3ReturnToR3ReturnCommand(r3Return);
            String body = "";
            String messageTitle = returnName +  "firmy " + r3Return.getNameOwner() + " za " + r3Return.getReturnDate().toString().substring(5, 7) + "/" + r3Return.getReturnDate().getYear();
            if (r3Return.getTax() != 0) {
                body = executeAndCompileMustacheTemplate("template/" + returnName + "Return.mustache", r3ReturnCommand) + footer;
            } else {
                body = executeAndCompileMustacheTemplate("template/"  + returnName + "ReturnNoTax.mustache", r3ReturnCommand) + footer;
            }
            mailDetails = MailsUtility.createMailDetails(messageTitle, body, bccEmail, toEmail);
        }
        return mailDetails;
    }

    private Boolean ifVat(R3Return r3Return){
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


    private void saveSatausReturn( R3Return r3Return, Boolean tempStatus){
        r3Return.setEmailSent(tempStatus);
        if(tempStatus==false) {
            DateTimeFormatter sendingEmailFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            r3Return.setEmailDataSent(LocalDate.parse(now().format(sendingEmailFormater)));
        }
        r3ReturnRepository.save(r3Return);
    }

    private Boolean isReturnShouldBeSend(R3Return r3Return, String nameReturn){
        if(ifCit(r3Return) ||ifPit(r3Return) || ifRyczalt(r3Return)) {
            return useOtherRemainingFile.checkOtherRemainingFile(nameReturn, r3Return);
        } else if (ifVat(r3Return)){
            return  r3Return.getEReturnStatusProcess() == 4 || r3Return.getEReturnStatusProcess() == 2;
        } else{
            return false;
        }
    }
}
