package pl.szmaus.firebirdf00154.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.szmaus.firebirdf00154.entity.SalesInvoice;
import pl.szmaus.firebirdf00154.repository.SalesInvoiceRepository;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetSalesInvoiceImp implements GetSalesInvoice {

    private final SalesInvoiceRepository salesInvoiceRepository;

    public GetSalesInvoiceImp(SalesInvoiceRepository salesInvoiceRepository) {
        this.salesInvoiceRepository = salesInvoiceRepository;
    }

    @Transactional
    public List<SalesInvoice> issuedInvoicesList(Month month, Integer year) {
            return salesInvoiceRepository.findAll()
                    .stream()
                    .filter(e->e.getIssueInvoiceDate().getMonth().equals(month) && e.getIssueInvoiceDate().getYear() == year)
                    .collect(Collectors.toList());
    }

    @Transactional
    public void setStatusInvoice(SalesInvoice salesInvoice, String status){
        salesInvoice.setStatus(status);
        salesInvoiceRepository.save(salesInvoice);
    }

}