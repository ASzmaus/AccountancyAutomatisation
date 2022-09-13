package pl.szmaus.firebirdraks3000.service;

import pl.szmaus.firebirdraks3000.entity.R3Return;

import java.time.Month;
import java.util.List;

public interface ReturnR3Declaration {

    public List<R3Return> monthR3ReturnList(Month month, Integer year);

    public  void saveSatausReturn( R3Return r3Return, Boolean tempStatus);

    public Boolean isR3ReturnSend(R3Return r3Return);

    public Boolean ifVat(R3Return r3Return);
    public Boolean ifPit(R3Return r3Return);
    public Boolean ifCit(R3Return r3Return);
    public Boolean ifRyczalt(R3Return r3Return);
    public String getReturnType(R3Return r3Return);
    public String  switchTaxReturnNo(int taxNumber);
}
