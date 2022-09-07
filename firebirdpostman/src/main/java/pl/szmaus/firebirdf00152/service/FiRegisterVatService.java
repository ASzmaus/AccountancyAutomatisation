package pl.szmaus.firebirdf00152.service;

import org.springframework.stereotype.Service;
import pl.szmaus.firebirdf00152.entity.FiRegisterVat;
import pl.szmaus.firebirdf00152.repository.FiRegisterVatRepository;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;

@Service
public class FiRegisterVatService {
    private static final Long VAT_COLUMN = 50L;
    private static final String VAT_TYPE ="N";
    private static final String VAT_REGISTER= "Rejestr Sprzedaży";
    private static final Long VAT_REGISTER_ID = 1L;
    private static final String USER_NAME = "HELPER";
    private static final Long EDIT_TIME = 0L;
    private final FiRegisterVatRepository fiRegisterVatRepository;

    public FiRegisterVatService(FiRegisterVatRepository fiRegisterVatRepository) {
        this.fiRegisterVatRepository = fiRegisterVatRepository;
    }
    public FiRegisterVat createFiRegisterVat(String[] arrayRecord, Long idR3AccountDocument) {
       FiRegisterVat fiRegisterVat = fiRegisterVatRepository.findByAccountDocId(idR3AccountDocument);
       if (fiRegisterVat==null){
           fiRegisterVat = new FiRegisterVat();
           fiRegisterVat.setId(fiRegisterVatRepository.getNextValFI_REJESTR_VAT_ID_GEN());
           fiRegisterVat.setAccountDocId(idR3AccountDocument);
           fiRegisterVat.setVatType(VAT_TYPE);
           fiRegisterVat.setVatRegister(VAT_REGISTER);
           fiRegisterVat.setVatColumn(VAT_COLUMN);
           fiRegisterVat.setImported(true);
           fiRegisterVat.setVatRegisterId(VAT_REGISTER_ID);
           fiRegisterVat.setVatDate(LocalDate.parse(arrayRecord[1]));
           fiRegisterVat.setSaleDate(LocalDate.parse(arrayRecord[2]));
           fiRegisterVat.setSalesGrossMargin(new BigDecimal(0));
           fiRegisterVat.setPurchaseGrossMargin(new BigDecimal(0));
           fiRegisterVat.setCIdentUser(USER_NAME);
           fiRegisterVat.setCDate(now());
           fiRegisterVat.setEditTime(EDIT_TIME);
       }

       fiRegisterVatRepository.save(fiRegisterVat);
       return fiRegisterVat;
    }
}
