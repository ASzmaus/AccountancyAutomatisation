package pl.szmaus.configuration;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Log4J2PropertiesConf {

    public void logSentMail(String toEmail, String bccEmail, String mailDetailsGetMailTitle, String mailDetailsGetMailBody){
        log.info("mail sent to {} {}; title{}; content:{} ", toEmail, bccEmail, mailDetailsGetMailTitle, mailDetailsGetMailBody);
    }

    public void logErrorSendEmail(String mailDetailsGetMailTitle, Exception e) {
        log.error("This is a message error sending email {} ", mailDetailsGetMailTitle, e);
    }

    public void logInvImport(String numberOfInvoice ) {
       log.info("Info - import invoice {} ", numberOfInvoice);
    }
    public void logNotImportInv(String numberOfInvoice ) {
        log.info("Not import invoice {} , check if inv is booked in Raks", numberOfInvoice);
    }

    public void logImportError(String numberOfInvoice, Exception e) {
        log.error("This is a message error durring import invoice {} ", numberOfInvoice, e);
    }

    public void logImportError( Exception e) {
        log.error("This is a message error durring import invoice ", e);
    }
    public void logErrorForCsvFile(String fileName, Exception e) {
        log.error("This is a message error durring parse csv file {} ", fileName, e);
    }

}
