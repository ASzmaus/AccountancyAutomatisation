package pl.szmaus.configuration;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "scheduling")
public class ScheduleConfiguration {
   private Integer paymentDate;
   private Integer paymentDayInInvoiceReminder;
   private Integer paymentInvoiceDebtCollection;
   private Integer reminder1Documents;
   private Integer reminder2Documents;
   private Integer reminder3Documents;
   private String interestRate;
   private String cronOutstandingInvoice;
   private String cronIssuedInvoice;
   private String cronDeliveryOfDocuments;
   private Boolean enabled;
   private String cronReminderForClient;
   private String cronQRCode;
   private String cronReturns;
   private String cronReturnsAdminEmail;
   private LocalDate currentDate;
   private Integer lastReminderDay;
}
