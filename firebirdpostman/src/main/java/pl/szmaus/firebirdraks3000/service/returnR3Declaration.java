package pl.szmaus.firebirdraks3000.service;

import pl.szmaus.firebirdraks3000.entity.R3Return;

import java.time.Month;
import java.util.List;

public interface returnR3Declaration {
    public List<R3Return> monthR3ReturnList(Month month, Integer year);
}