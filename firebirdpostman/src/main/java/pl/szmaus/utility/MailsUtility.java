package pl.szmaus.utility;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class MailsUtility {

    public static MailDetails createMailDetails(String mailTitle, String mailBody, String bccEmail, String toEmail, byte[] attachmentInvoice, Map<String, byte[]> imagesMap){
        MailDetails mailDetails = new MailDetails();
        mailDetails =  setEmailBodyTitleInMailDetails( mailDetails, mailTitle, mailBody, bccEmail, toEmail);
        if(attachmentInvoice!=null)
            mailDetails.setAttachmentInvoice(attachmentInvoice);
        if(imagesMap!=null)
            mailDetails.setImagesMap(imagesMap);
        return mailDetails;
    }

    public static MailDetails createMailDetails(String mailTitle, String mailBody, String bccEmail, String toEmail){
        return createMailDetails(mailTitle, mailBody,  bccEmail, toEmail, null,null);
    }

    private static MailDetails setEmailBodyTitleInMailDetails(MailDetails mailDetails, String mailTitle, String mailBody, String bccEmail, String toEmail) {
        mailDetails.setMailBody(mailBody);
        mailDetails.setMailTitle(mailTitle);
        mailDetails.setBccEmail(bccEmail);
        mailDetails.setToEmail(toEmail);
        return mailDetails;
    }
}
