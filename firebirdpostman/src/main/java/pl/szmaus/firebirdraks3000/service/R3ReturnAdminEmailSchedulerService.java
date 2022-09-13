package pl.szmaus.firebirdraks3000.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.utility.MailsUtility;
import static java.time.LocalDate.now;

@Log4j2
@Service
public class R3ReturnAdminEmailSchedulerService extends AbstractMailDetails {

    private static final Integer CURRENT_RETURN_MONTH = 1;
    private final ReturnR3Declaration returnR3Declaration;

    public R3ReturnAdminEmailSchedulerService(
        ReturnR3Declaration returnR3Declaration,
        ScheduleConfiguration scheduleConfiguration,
        SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.returnR3Declaration = returnR3Declaration;
    }

    @Scheduled(cron = "${scheduling.cronReturnsAdminEmail}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            returnR3Declaration.monthR3ReturnList(now().getMonth().minus(CURRENT_RETURN_MONTH), now().getYear())
                .stream()
                .filter(p-> returnR3Declaration.isR3ReturnSend(p))
                .forEach(d -> {
                    String nameOfTaxReturn= returnR3Declaration.switchTaxReturnNo(d.getId_definition_return());
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
                        mailDetails = MailsUtility.createMailDetails(
                                d.getNameOwner()+ " nie ma NIP-u na deklaracji " + nameOfTaxReturn + " w Raks",
                                executeAndCompileMustacheTemplate("template/noTaxId.mustache",d) + footer,
                                bccEmail, toEmail);
                    }else if(getCompany.ifLackOfInformationInCompany(d.getNip())) {
                            tempStatus=true;
                            mailDetails = getCompany.checkEmailAndTaxId(d.getNip(), d.getNameOwner());
                    }
                    if(isAdminEmailShouldBeSend(d)) {
                        sendEmailMicrosoft.configurationMicrosoft365Email(
                                mailDetails.getToEmail(),
                                mailDetails.getBccEmail(),
                                mailDetails.getMailBody(),
                                mailDetails.getMailTitle(),
                                mailDetails.getAttachmentInvoice(),
                                mailDetails.getImagesMap());
                        log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                        returnR3Declaration.saveSatausReturn(d, tempStatus);
                    }
                });
        } catch (Exception e) {
            log.error(e);
        }
    }

    private Boolean isAdminEmailShouldBeSend(R3Return r3Return){
        return mailDetails!=null && r3Return.getEmailSent()==false  && (r3Return.getNip() ==null  || r3Return.getNip()!=null && getCompany.ifLackOfInformationInCompany(r3Return.getNip()));
    }
}
