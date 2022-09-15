package pl.szmaus.firebirdf00152.service;

import org.springframework.stereotype.Service;
import pl.szmaus.firebirdf00152.entity.KRDocumentBook;
import pl.szmaus.firebirdf00152.repository.KRDocumentBookRepository;
import java.math.BigDecimal;
import static java.time.LocalDateTime.now;

@Service
public class KRDocumentBookService {
    private static final Long ID_YEAR = 203L;
    private static final Long RECORD_TYPE =7L;
    private static final String DESCRIPTION_OF_OPERATION= "SPRZEDAŻ USŁUG";
    private static final String CURRENCY = "PLN";
    private static final String CURRENCY_ADDITIONAL = "PLN";
    private static final String USER_NAME = "HELPER";
    private static final Long EDIT_TIME = 0L;
    private final KRDocumentBookRepository krDocumentBookRepository;

    public KRDocumentBookService(KRDocumentBookRepository krDocumentBookRepository) {
        this.krDocumentBookRepository = krDocumentBookRepository;
    }

    public KRDocumentBook createKrDocumentBook(String[] arrayRecord, Long idR3AccountDocument){
        KRDocumentBook krDocumentBook = krDocumentBookRepository.findByAccountDocId(idR3AccountDocument);
        if(krDocumentBook==null) {
            krDocumentBook = new KRDocumentBook();
            krDocumentBook.setId(krDocumentBookRepository.getNextValKR_DOKUMENTY_KSIEGA_ID_GEN());
            krDocumentBook.setIdYear(ID_YEAR);
            krDocumentBook.setAccountDocId(idR3AccountDocument);
            krDocumentBook.setRecordType(RECORD_TYPE);
            krDocumentBook.setAmount(new BigDecimal(arrayRecord[7].replaceAll(",", ".")));
            krDocumentBook.setAmountAdditional(new BigDecimal(0));
            krDocumentBook.setDescriptionOfOperation(DESCRIPTION_OF_OPERATION);
            krDocumentBook.setImported(true);
            krDocumentBook.setCurrency(CURRENCY);
            krDocumentBook.setCurrencyAdditional(CURRENCY_ADDITIONAL);
            krDocumentBook.setAmountAddCurrency(new BigDecimal(0));
            krDocumentBook.setAmountCurrency(new BigDecimal(0));
            krDocumentBook.setCIdentUser(USER_NAME);
            krDocumentBook.setCDate(now());
            krDocumentBook.setEditTime(EDIT_TIME);
        } else {
            krDocumentBook.setAmount(krDocumentBook.getAmount().add(new BigDecimal(arrayRecord[7].replaceAll(",", "."))));
        }
        krDocumentBookRepository.save(krDocumentBook);
        return krDocumentBook;
    }
}
