package pl.szmaus.firebirdraks3000.mapper;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import pl.szmaus.firebirdraks3000.command.R3ReturnCommand;
import pl.szmaus.firebirdraks3000.entity.R3Jpk;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.repository.R3JpkRepository;
import pl.szmaus.firebirdraks3000.service.ReadXmlDomParser;
import pl.szmaus.firebirdraks3000.service.UnzipInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

@Log4j2
@Component
public class R3ReturnMapper {

    private final R3JpkRepository r3JpkRepository;
    private final UnzipInputStream unzipInputStream;
    private final ReadXmlDomParser readXmlDomParser;
    private static final int JPK_V7K_NO =702;
    private static final int JPK_V7M_NO =703;

    public R3ReturnMapper( R3JpkRepository r3JpkRepository, UnzipInputStream unzipInputStream, ReadXmlDomParser readXmlDomParser) {
        this.r3JpkRepository = r3JpkRepository;
        this.unzipInputStream = unzipInputStream;
        this.readXmlDomParser = readXmlDomParser;
    }


    public R3ReturnCommand mapR3ReturnToR3ReturnCommand(R3Return r3Return){
        return R3ReturnCommand
                .builder()
                .nameOwner(r3Return.getNameOwner())
                .settlementPeriod(r3Return.getReturnDate().toString().substring(5, 7) + "/" + r3Return.getReturnDate().getYear())
                .tax(r3Return.getTax())
                .dueDatePit( LocalDate.of(r3Return.getReturnDate().getYear(), r3Return.getReturnDate().getMonth(), 20).plusMonths(1))
                .dueDateRyczalt( LocalDate.of(r3Return.getReturnDate().getYear(), r3Return.getReturnDate().getMonth(), 20).plusMonths(1))
                .dueDateCit( LocalDate.of(r3Return.getReturnDate().getYear(), r3Return.getReturnDate().getMonth(), 20).plusMonths(1))
                .dueDateVat(LocalDate.of(r3Return.getReturnDate().getYear(), r3Return.getReturnDate().getMonth(), 25).plusMonths(1))
                .vatOverPayment( ((r3Return.getId_definition_return() == JPK_V7M_NO || r3Return.getId_definition_return() == JPK_V7K_NO) && r3Return.getTax()==0 ) ? calculateVatOverpayment(r3Return) : "0" )
                .build();
    }

    private String calculateVatOverpayment(R3Return r3Return){
        R3Jpk r3Jpk = r3JpkRepository.findByIdR3Return(r3Return.getId());
        byte[] bytesJpk = r3Jpk.getFile();
        String vatOverPayment = "0";
        InputStream jpkZipInputStream = new ByteArrayInputStream(bytesJpk);
        try {
            InputStream jpkUnzipInputStream = unzipInputStream.unzip(jpkZipInputStream);
            vatOverPayment = readXmlDomParser.readFromXmlDomParser(jpkUnzipInputStream, r3Return.getId_definition_return());
        } catch (IOException e) {
            log.error(e);
        }
        return vatOverPayment;
    }
}
