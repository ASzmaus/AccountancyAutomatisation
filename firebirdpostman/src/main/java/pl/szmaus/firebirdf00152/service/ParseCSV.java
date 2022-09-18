package pl.szmaus.firebirdf00152.service;

import com.microsoft.graph.http.GraphServiceException;
import org.springframework.stereotype.Service;
import pl.szmaus.API.MicrosoftGraphAPI;
import pl.szmaus.configuration.ImportConfiguration;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.firebirdf00152.repository.R3AccountDocumentRepository;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Service
public class ParseCSV {

    private final R3AccountDocumentRepository r3AccountDocumentRepository;
    private final MicrosoftGraphAPI microsoftGraphAPI;
    private final ImportConfiguration importConfiguration;

    public ParseCSV(R3AccountDocumentRepository r3AccountDocumentRepository, MicrosoftGraphAPI microsoftGraphAPI, ImportConfiguration importConfiguration) {
        this.r3AccountDocumentRepository = r3AccountDocumentRepository;
        this.microsoftGraphAPI = microsoftGraphAPI;
        this.importConfiguration = importConfiguration;
    }

    public List<String[]> redFile(String nameCsvFilePath) throws GraphServiceException {
        List<String[]> listOfRecordsFromCsv = new LinkedList<>();
        String splitBy = ";";
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try {
                String pathToResourceSite = importConfiguration.getPathToResourceSite();
                BufferedReader br = new BufferedReader(new InputStreamReader(microsoftGraphAPI.getContentMyFile(pathToResourceSite,nameCsvFilePath), "UTF-8"));
                br.readLine();
                String line = "";
                while ((line = br.readLine()) != null) {
                    String[] invoice = line.split(splitBy);  //use semicolen as separator
                    if (r3AccountDocumentRepository.findByDocumentNumber(invoice[0]) == null) {
                        listOfRecordsFromCsv.add(invoice);
                    } else{
                        log4J2PropertiesConf.logNotImportInv(invoice[0]);
                    }
                }
        } catch (IOException e) {
            log4J2PropertiesConf.logErrorForCsvFile(nameCsvFilePath, e);
        }
       return listOfRecordsFromCsv;
    }
}
