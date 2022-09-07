package pl.szmaus.utility;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MailsUtility {

    public static MailDetails createMailDetails(String mailTitle, String mailBody, String bccEmail, String toEmail, byte[] attachmentInvoice, Map<String, byte[]> imagesMap){
        MailDetails mailDetails = new MailDetails();
        mailDetails.setMailBody(mailBody);
        mailDetails.setMailTitle(mailTitle);
        mailDetails.setBccEmail(bccEmail);
        mailDetails.setToEmail(toEmail);
        if(attachmentInvoice!=null)
            mailDetails.setAttachmentInvoice(attachmentInvoice);
        if(imagesMap!=null)
            mailDetails.setImagesMap(imagesMap);
        return mailDetails;
    }
}
