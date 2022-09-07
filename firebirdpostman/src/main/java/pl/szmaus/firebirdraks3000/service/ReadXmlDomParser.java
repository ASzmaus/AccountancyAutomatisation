package pl.szmaus.firebirdraks3000.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

@Service
@Log4j2
public class ReadXmlDomParser {
    private static final int JPK_V7K_NO = 702;
    private static final int JPK_V7M_NO = 703;
    private static final int VAT_AMOUNT_INDEX = 0;
    private static final String VAT_AMOUNT_FIELD = "P_53";
    private static final String VAT_RETURN_NAME = "JPK";
    private static final String VAT_SPACE_URI_V7M = "http://crd.gov.pl/wzor/2021/12/27/11148/";
    private static final String VAT_SPACE_URI_V7K = "http://crd.gov.pl/wzor/2021/12/27/11149/";

    public String readFromXmlDomParser(InputStream is, Integer versionOfJpk) {
        String valueOfFieldFromReturn ="0";
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            if(versionOfJpk==JPK_V7M_NO ) {
                valueOfFieldFromReturn = readFieldValueFromReturn(doc, VAT_SPACE_URI_V7M, VAT_RETURN_NAME, VAT_AMOUNT_FIELD, VAT_AMOUNT_INDEX);
            }else if(versionOfJpk==JPK_V7K_NO) {
                valueOfFieldFromReturn = readFieldValueFromReturn(doc, VAT_SPACE_URI_V7K, VAT_RETURN_NAME, VAT_AMOUNT_FIELD, VAT_AMOUNT_INDEX);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return valueOfFieldFromReturn;
    }
    private String readFieldValueFromReturn(Document doc, String nameSpaceUri,String returnName, String fieldName, Integer index){
        String returnValue="0";
        NodeList listJpkM = doc.getElementsByTagNameNS( nameSpaceUri, returnName);
        for (int i = 0; i < listJpkM.getLength(); i++) {
            Element element = (Element) listJpkM.item(i);
            Element returnField = (Element) element.getElementsByTagNameNS(
                    nameSpaceUri, fieldName).item(index);
            if(returnField==null || returnField.getTextContent()==null) {
                return returnValue;
            } else {
                returnValue = returnField.getTextContent();
            }
        }
        return  returnValue;
    }
}

