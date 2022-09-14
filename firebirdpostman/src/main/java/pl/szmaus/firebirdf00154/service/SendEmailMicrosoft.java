package pl.szmaus.firebirdf00154.service;

import java.util.Map;

public interface SendEmailMicrosoft {
    public void configurationMicrosoft365Email(String toEmail, String bccEmail, String htmlText, String subject, byte[] data, Map<String,byte[]> imagesMap);
    public void configurationMicrosoft365Email(String toEmail, String bccEmail, String htmlText, String subject);
}
