package pl.szmaus.abstarct;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.MailService;
import pl.szmaus.firebirdf00154.service.SendingEmailMicrosoft;
import pl.szmaus.firebirdraks3000.service.CompanyService;
import pl.szmaus.utility.MailDetails;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Data
@Log4j2
@Service
@ConditionalOnProperty(value="scheduling.enabled", havingValue="true", matchIfMissing = true)
public abstract class AbstractMailDetails {

    protected final ScheduleConfiguration scheduleConfiguration;
    protected final SendingEmailMicrosoft sendingEmailMicrosoft;
    protected final MailConfiguration mailConfiguration;
    protected final CompanyService companyService;
    protected MailDetails mailDetails;
    protected Map<String,byte[]> imagesMap = new HashMap<String,byte[]>();
    protected byte[] emailAttachment= null;
    protected final String footer = executeAndCompileMustacheTemplate("template/footer.mustache",this);

    public AbstractMailDetails(ScheduleConfiguration scheduleConfiguration, SendingEmailMicrosoft sendingEmailMicrosoft, MailConfiguration mailConfiguration, CompanyService companyService) {
        this.scheduleConfiguration = scheduleConfiguration;
        this.sendingEmailMicrosoft = sendingEmailMicrosoft;
        this.mailConfiguration = mailConfiguration;
        this.companyService = companyService;
    }

    protected void trackSendEmail() {
        log.info("Beginning of scheduler trackIssuedSalesInvoices");
        companyService.verificationIfTaxIdIsValid();
    }

    protected String executeAndCompileMustacheTemplate(String pathTemplateName, Object object){
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mTemplate = mf.compile(pathTemplateName);
        StringWriter writerTemplate = new StringWriter();
        try {
            mTemplate.execute(writerTemplate, object).flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writerTemplate.toString();
    }
}
