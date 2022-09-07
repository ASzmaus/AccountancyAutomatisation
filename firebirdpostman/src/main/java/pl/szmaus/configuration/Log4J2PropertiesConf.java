package pl.szmaus.configuration;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Log4J2PropertiesConf {

    public void performSomeTask( String toEmail, String bccEmail, String mailDetailsGetMailTitle, String mailDetailsGetMailBody){
        log.info("mail sent to {} {}; title{}; content:{} ", toEmail, bccEmail, mailDetailsGetMailTitle, mailDetailsGetMailBody);
    }

    public void performSendingInv( String mailDetailsGetMailTitle, Exception e) {
        log.error("This is a message error sending email {} ", mailDetailsGetMailTitle, e);
    }

    public void performImport( String numberOfInvoice ) {
       log.info("Info - import invoice {} ", numberOfInvoice);
    }

    public void performImportError( String numberOfInvoice, Exception e) {
        log.error("This is a message error durring import invoice {} ", numberOfInvoice, e);
    }

    public void performParseCSV( String fileName, Exception e) {
        log.error("This is a message error durring parse csv file {} ", fileName, e);
    }
}
