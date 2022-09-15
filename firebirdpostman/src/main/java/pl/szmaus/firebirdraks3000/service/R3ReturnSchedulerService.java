package pl.szmaus.firebirdraks3000.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.szmaus.abstarct.AbstractMailDetails;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.UseOtherRemainingFile;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.command.R3ReturnCommand;
import pl.szmaus.firebirdraks3000.entity.Company;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.mapper.R3ReturnMapper;
import pl.szmaus.firebirdraks3000.repository.R3ReturnRepository;
import pl.szmaus.utility.MailDetails;
import pl.szmaus.utility.MailsUtility;
import java.util.List;

import static java.time.LocalDate.now;
@Log4j2
@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public class R3ReturnSchedulerService extends AbstractMailDetails {

    private static final Integer CURRENT_RETURN_MONTH = 1;
    private final ReturnR3Declaration returnR3Declaration;
    private final UseOtherRemainingFile useOtherRemainingFile;
    private final R3ReturnMapper r3ReturnMapper;

    public R3ReturnSchedulerService(
        ReturnR3Declaration returnR3Declaration,
        UseOtherRemainingFile useOtherRemainingFile,
        R3ReturnMapper r3ReturnMapper,
        ScheduleConfiguration scheduleConfiguration,
        SendEmailMicrosoft sendEmailMicrosoft,
        MailConfiguration mailConfiguration,
        GetCompany getCompany) {
        super(scheduleConfiguration, sendEmailMicrosoft, mailConfiguration, getCompany);
        this.returnR3Declaration = returnR3Declaration;
        this.useOtherRemainingFile = useOtherRemainingFile;
        this.r3ReturnMapper = r3ReturnMapper;
    }

    @Scheduled(cron = "${scheduling.cronReturns}")
    public void trackSendEmail() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try{
            returnR3Declaration.monthR3ReturnList(now().getMonth().minus(CURRENT_RETURN_MONTH), now().getYear())
                .stream()
                .filter(p-> returnR3Declaration.isR3ReturnSend(p))
                .forEach(d -> {
                    List<Company> companyList = getCompany.findListCompanyFindByTaxId(d.getNip());
                    if(d.getNip()!=null && !getCompany.ifLackOfInformationInCompany(d.getNip())) {
                        Boolean tempStatus = false;
                        if (returnR3Declaration.ifVat(d)) {
                            mailDetails = returnR3MailDetails(d, "VAT", companyList);
                        }  else {
                            if (useOtherRemainingFile.checkOtherRemainingFile(returnR3Declaration.getReturnType(d), d) == true)
                                mailDetails = returnR3MailDetails(d, returnR3Declaration.getReturnType(d), companyList);
                        }

                        if( d.getEmailSent()==false && mailDetails!=null) {
                            sendEmailMicrosoft.configurationMicrosoft365Email(
                                mailDetails.getToEmail(),
                                mailDetails.getBccEmail(),
                                mailDetails.getMailBody(),
                                mailDetails.getMailTitle());
                            log4J2PropertiesConf.performSomeTask(mailDetails.getToEmail(), mailDetails.getBccEmail(), mailDetails.getMailTitle(), mailDetails.getMailBody());
                            returnR3Declaration.saveSatausReturn(d, tempStatus);
                            mailDetails=null;
                        }
                    }
                });
        } catch (Exception e) {
            log.error(e);
        }
    }

    private MailDetails returnR3MailDetails(R3Return r3Return, String returnName, List<Company> companyList){
        String toEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? getCompany.returnCompanyEmails(companyList.get(0)) : mailConfiguration.getToEmail();
        String bccEmail = mailConfiguration.getBlockToEmailProd().equals(false) ? mailConfiguration.getBccEmailTax() : mailConfiguration.getBccEmail();
        if (isReturnShouldBeSend(r3Return, returnName)) {
            R3ReturnCommand r3ReturnCommand = r3ReturnMapper.mapR3ReturnToR3ReturnCommand(r3Return);
            String messageTitle = returnName +  " firmy " + r3Return.getNameOwner() + " za " + r3Return.getReturnDate().toString().substring(5, 7) + "/" + r3Return.getReturnDate().getYear();
            String body = "";
            if (r3Return.getTax() != 0) {
                body = executeAndCompileMustacheTemplate("template/" + returnName + "Return.mustache", r3ReturnCommand) + footer;
            } else {
                body = executeAndCompileMustacheTemplate("template/"  + returnName + "ReturnNoTax.mustache", r3ReturnCommand) + footer;
            }
            mailDetails = MailsUtility.createMailDetails(messageTitle, body, bccEmail, toEmail);
        }
        return mailDetails;
    }

    private Boolean isReturnShouldBeSend(R3Return r3Return, String nameReturn){
        if(returnR3Declaration.ifCit(r3Return) || returnR3Declaration.ifPit(r3Return) || returnR3Declaration.ifRyczalt(r3Return)) {
            return useOtherRemainingFile.checkOtherRemainingFile(nameReturn, r3Return);
        } else if (returnR3Declaration.ifVat(r3Return)){
            return  r3Return.getEReturnStatusProcess() == 4 || r3Return.getEReturnStatusProcess() == 2;
        } else {
            return false;
        }
    }
}
