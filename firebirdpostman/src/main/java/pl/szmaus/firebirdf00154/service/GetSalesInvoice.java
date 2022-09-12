package pl.szmaus.firebirdf00154.service;

import pl.szmaus.firebirdf00154.entity.SalesInvoice;

import java.time.Month;
import java.util.List;

public interface GetSalesInvoice {

   public List<SalesInvoice> issuedInvoicesList(Month month, Integer year);

   public void setStatusInvoice(SalesInvoice salesInvoice, String status);

}
