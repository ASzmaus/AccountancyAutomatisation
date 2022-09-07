package pl.szmaus.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "mailing")
public class MailConfiguration {

    private String fromEmail;
    private String toEmailProd;
    private String toEmail;
    private String bccEmail;
    private String toEmailClient;
    private String bccEmailClient;
    private String toEmailDocClient;
    private String bccEmailDocClient;
    private String toEmailTax;
    private String bccEmailTax;
    private String toEmailIt;
    private String bccEmailIt;
    private String password;
    private String mailSmtpHost;
    private String mailSmtpPort;
    private String mailSmtpAuth;
    private String mailSmtpStarttlsEnable;
    private String codingSystem;
    private Boolean blockToEmailProd;
    private String officeBankAccount;
    private String accountingOffice;
    private String user;
    private String clientId;
    private String clientSecret;
    private String tenantId;

}
