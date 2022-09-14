package pl.szmaus.firebirdraks3000.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.repository.R3ReturnRepository;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import static java.time.LocalDate.now;

@Service
public class ReturnR3DeclarationImp implements ReturnR3Declaration {

    public static final String PODATEK_LINIOWY ="podatek liniowy";
    public static final String SKALA_PODATKOWA ="skala podatkowa";
    public static final String ZRYCZALTOWANY_PODATEK ="zryczaltowany podatek";
    public static final int JPK_V7K_NO =702;
    public static final int JPK_V7M_NO =703;
    public static final int CIT8_NO = 63;
    public static final int RYCZALTM_NO = 735;
    public static final int RYCZALTK_NO = 734;
    public static final int PIT5L_NO = 730;
    public static final int PIT5_NO = 733;
    private final R3ReturnRepository r3ReturnRepository;

    public ReturnR3DeclarationImp(R3ReturnRepository r3ReturnRepository) {
        this.r3ReturnRepository = r3ReturnRepository;
    }

    @Transactional
    public List<R3Return> monthR3ReturnList(Month month, Integer year) {
        return r3ReturnRepository.findAll()
                .stream()
                .filter(e->e.getReturnDate().getMonth().equals(month) && e.getReturnDate().getYear() == year)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveSatausReturn( R3Return r3Return, Boolean tempStatus){
        if(tempStatus== true){
            r3Return.setEmailSent(tempStatus);
        } else if(tempStatus==false) {
            DateTimeFormatter sendingEmailFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            r3Return.setEmailDataSent(LocalDate.parse(now().format(sendingEmailFormater)));
        }
        r3ReturnRepository.save(r3Return);
    }

    public Boolean isR3ReturnSend(R3Return r3Return){
        return r3Return.getEmailDataSent()==null && (ifVat(r3Return) || ifCit(r3Return) || ifPit(r3Return) || ifRyczalt(r3Return));
    }

    public Boolean ifVat(R3Return r3Return){
        return r3Return.getId_definition_return() == JPK_V7M_NO || r3Return.getId_definition_return() == JPK_V7K_NO;
    }
    public Boolean ifPit(R3Return r3Return){
        return r3Return.getId_definition_return() == PIT5L_NO || r3Return.getId_definition_return() == PIT5_NO;
    }

    public Boolean ifCit(R3Return r3Return){
        return r3Return.getId_definition_return() == CIT8_NO;
    }

    public Boolean ifRyczalt(R3Return r3Return){
        return r3Return.getId_definition_return() == RYCZALTM_NO || r3Return.getId_definition_return() == RYCZALTK_NO;
    }

    public String getReturnType(R3Return r3Return){
        if(ifVat(r3Return)){
            return "VAT";
        } else if(ifCit(r3Return)){
            return "CIT";
        } else if(ifPit(r3Return)){
            return "PIT";
        } else if(ifRyczalt(r3Return)){
            return "RYCZALT";
        }
        return "unknown RETURN";
    }

    public String  switchTaxReturnNo(int taxNumber){
        String nameOfTaxRetur ="";
        switch (taxNumber) {
            case PIT5L_NO:
                nameOfTaxRetur = PODATEK_LINIOWY;
                break;
            case PIT5_NO:
                nameOfTaxRetur = SKALA_PODATKOWA;
                break;
            case RYCZALTK_NO:
                nameOfTaxRetur = ZRYCZALTOWANY_PODATEK;
                break;
            case RYCZALTM_NO:
                nameOfTaxRetur = ZRYCZALTOWANY_PODATEK;
                break;
            default:
                nameOfTaxRetur = "unknown RETURN";
                break;
        }
        return nameOfTaxRetur;
    }

}
