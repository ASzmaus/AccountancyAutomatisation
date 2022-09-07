package pl.szmaus.firebirdf00152.service;

import org.springframework.stereotype.Service;
import pl.szmaus.firebirdf00152.entity.FiRegisterVatOther;
import pl.szmaus.firebirdf00152.repository.FiRegisterVatOtherRepository;


import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;

@Service
public class FiRegisterVatOtherService {
    private static final Long VAT_RATE_ID = 12L;
    private static final String CORE_TYPE ="B";
    private static final String USER_NAME = "HELPER";
    private static final Long EDIT_TIME = 0L;
    private final FiRegisterVatOtherRepository fiRegisterVatOtherRepository;

    public FiRegisterVatOtherService( FiRegisterVatOtherRepository fiRegisterVatOtherRepository) {
        this.fiRegisterVatOtherRepository = fiRegisterVatOtherRepository;
    }

    public FiRegisterVatOther createFiRegisterVatOther(String[] arrayRecord, Long idFiRegisterVat)  {
       FiRegisterVatOther fiRegisterVatOther  = fiRegisterVatOtherRepository.findByVatId(idFiRegisterVat);
       if(fiRegisterVatOther==null){
           fiRegisterVatOther = new FiRegisterVatOther();
           fiRegisterVatOther.setId(fiRegisterVatOtherRepository.getNextValFI_REJESTR_VAT_POZ_ID_GEN());
           fiRegisterVatOther.setVatId(idFiRegisterVat);
           fiRegisterVatOther.setVatRateId(VAT_RATE_ID);
           fiRegisterVatOther.setVatRateValue(arrayRecord[6]);
           fiRegisterVatOther.setAmountNet(new BigDecimal(arrayRecord[7].replaceAll(",", ".")));
           fiRegisterVatOther.setAmountGross(new BigDecimal(arrayRecord[8].replaceAll(",", ".")));
           fiRegisterVatOther.setAmuntVat(fiRegisterVatOther.getAmountGross().subtract(fiRegisterVatOther.getAmountNet()));
           fiRegisterVatOther.setImported(true);
           fiRegisterVatOther.setEditTime(EDIT_TIME);
           fiRegisterVatOther.setCoreType(CORE_TYPE);
           fiRegisterVatOther.setCIdentUser(USER_NAME);
           fiRegisterVatOther.setCDate(now());

       } else {
           fiRegisterVatOther.setAmountNet(fiRegisterVatOther.getAmountNet().add(new BigDecimal(arrayRecord[7].replaceAll(",", "."))));
           fiRegisterVatOther.setAmountGross(fiRegisterVatOther.getAmountGross().add(new BigDecimal(arrayRecord[8].replaceAll(",", "."))));
           fiRegisterVatOther.setAmuntVat(fiRegisterVatOther.getAmountGross().subtract(fiRegisterVatOther.getAmountNet()));
       }
        fiRegisterVatOtherRepository.save(fiRegisterVatOther);
        return fiRegisterVatOther;
    }
}
