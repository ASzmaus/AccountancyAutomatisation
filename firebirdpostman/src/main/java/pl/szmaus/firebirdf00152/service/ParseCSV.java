package pl.szmaus.firebirdf00152.service;

import org.springframework.stereotype.Service;
import pl.szmaus.configuration.Log4J2PropertiesConf;
import pl.szmaus.firebirdf00152.repository.R3AccountDocumentRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
public class ParseCSV {

    private final R3AccountDocumentRepository r3AccountDocumentRepository;

    public ParseCSV(R3AccountDocumentRepository r3AccountDocumentRepository) {
        this.r3AccountDocumentRepository = r3AccountDocumentRepository;
    }

    public List<String[]> redFile(String nameCsvFilePath) {
        List<String[]> listOfRecordsFromCsv = new LinkedList<>();
        String splitBy = ";";
        File file = new File(nameCsvFilePath);
        Log4J2PropertiesConf log4J2PropertiesConf = new Log4J2PropertiesConf();
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.readLine();
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] invoice = line.split(splitBy);  //use semicolen as separator
                if(r3AccountDocumentRepository.findByDocumentNumber(invoice[0])==null){
                    listOfRecordsFromCsv.add(invoice);
                }
            }
        } catch (IOException e) {
            log4J2PropertiesConf.performParseCSV( nameCsvFilePath, e);
        }
       return listOfRecordsFromCsv;
    }
}
