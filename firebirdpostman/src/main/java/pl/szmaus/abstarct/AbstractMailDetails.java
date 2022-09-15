package pl.szmaus.abstarct;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.stereotype.Service;
import pl.szmaus.configuration.MailConfiguration;
import pl.szmaus.configuration.ScheduleConfiguration;
import pl.szmaus.firebirdf00154.service.SendEmailMicrosoft;
import pl.szmaus.firebirdraks3000.service.GetCompany;
import pl.szmaus.utility.MailDetails;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public abstract class AbstractMailDetails {

    protected final ScheduleConfiguration scheduleConfiguration;
    protected final SendEmailMicrosoft sendEmailMicrosoft;
    protected final MailConfiguration mailConfiguration;
    protected final GetCompany getCompany;
    protected MailDetails mailDetails;
    protected Map<String, byte[]> imagesMap = new HashMap<String, byte[]>();
    protected byte[] emailAttachment = null;
    protected final String footer = executeAndCompileMustacheTemplate("template/footer.mustache", this);

    public AbstractMailDetails(ScheduleConfiguration scheduleConfiguration, SendEmailMicrosoft sendEmailMicrosoft, MailConfiguration mailConfiguration, GetCompany getCompany) {
        this.scheduleConfiguration = scheduleConfiguration;
        this.sendEmailMicrosoft = sendEmailMicrosoft;
        this.mailConfiguration = mailConfiguration;
        this.getCompany = getCompany;
    }

    abstract public void trackSendEmail();

    protected String executeAndCompileMustacheTemplate(String pathTemplateName, Object object) {
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
