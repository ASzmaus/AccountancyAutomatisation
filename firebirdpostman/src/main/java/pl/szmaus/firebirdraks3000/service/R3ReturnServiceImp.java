package pl.szmaus.firebirdraks3000.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdraks3000.entity.R3Return;
import pl.szmaus.firebirdraks3000.repository.R3ReturnRepository;

import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
public class R3ReturnServiceImp implements  R3ReturnService{
    private final R3ReturnRepository r3ReturnRepository;

    public R3ReturnServiceImp(R3ReturnRepository r3ReturnRepository) {
        this.r3ReturnRepository = r3ReturnRepository;
    }

    @Transactional
    public List<R3Return> monthR3ReturnList(Month month, Integer year) {
        return r3ReturnRepository.findAll()
                .stream()
                .filter(e->e.getReturnDate().getMonth().equals(month) && e.getReturnDate().getYear() == year)
                .collect(Collectors.toList());
    }
}