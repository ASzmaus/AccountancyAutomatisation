package pl.szmaus.firebirdf00152.service;

import org.apache.logging.log4j.core.util.Integers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.firebirdf00152.entity.FiRegisterVat;
import pl.szmaus.firebirdf00152.entity.FiRegisterVatOther;
import pl.szmaus.firebirdf00152.entity.KRDocumentBook;
import pl.szmaus.firebirdf00152.entity.R3AccountDocument;

import java.util.Comparator;
import java.util.List;

@Service
public class BookingSalesInvoiceService {
    private final R3AccountDocumentService r3AccountDocumentService;
    private final KRDocumentBookService krDocumentBookService;
    private final FiRegisterVatService fiRegisterVatService;
    private final FiRegisterVatOtherService fiRegisterVatOtherService;
    private final ParseCSV parseCSV;
    private static final String FILE_NAME_PATH = System.getProperty("user.dir") + "//zasoby//import//fs//kwiatekhol//Lista.csv";

    public BookingSalesInvoiceService(R3AccountDocumentService r3AccountDocumentService, KRDocumentBookService krDocumentBookService, FiRegisterVatService fiRegisterVatService, FiRegisterVatOtherService fiRegisterVatOtherService, ParseCSV parseCSV) {
        this.r3AccountDocumentService = r3AccountDocumentService;
        this.krDocumentBookService = krDocumentBookService;
        this.fiRegisterVatService = fiRegisterVatService;
        this.fiRegisterVatOtherService = fiRegisterVatOtherService;
        this.parseCSV = parseCSV;
    }

    public List<String[]> sortFile(List<String[]> listOfRecordsFromCsv) {
        listOfRecordsFromCsv.sort(Comparator.comparing(array -> Integers.parseInt(array[0].substring(0, array[0].indexOf("/")))));
        listOfRecordsFromCsv
                .stream()
                .forEach(invoice ->
                        System.out.println("INVOICE_NR=" + invoice[0] + ", ISSUE_DATE=" + invoice[1] + ", SELL_DATE=" + invoice[2] + ", SERVICE_NAME=" + invoice[3]));
        return listOfRecordsFromCsv;
    }

    @Transactional
    public void bookingSalesInvoiceInRaks() {
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        List<String[]> csvFileInvoices = parseCSV.redFile(FILE_NAME_PATH);
        csvFileInvoices = sortFile(csvFileInvoices);
        csvFileInvoices
                .stream()
                .forEach(arrayRecord -> {
                    try {
                          R3AccountDocument r3AccountDocument = r3AccountDocumentService.createR3AccountDocument(arrayRecord);
                          KRDocumentBook krDocumentBook = krDocumentBookService.createKrDocumentBook(arrayRecord, r3AccountDocument.getId());
                          FiRegisterVat fiRegisterVat = fiRegisterVatService.createFiRegisterVat(arrayRecord, r3AccountDocument.getId());
                          FiRegisterVatOther fiRegisterVatOther = fiRegisterVatOtherService.createFiRegisterVatOther(arrayRecord, fiRegisterVat.getId());
                          log4J2PropertiesConf.performImport(arrayRecord[0]);
                    } catch (Exception e) {
                          log4J2PropertiesConf.performImportError(arrayRecord[0], e);
                    }
                });
    }
}