package pl.szmaus.utility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data


public class MailDetails {
    private String mailBody;
    private String mailTitle;
    private String toEmail;
    private String bccEmail;
    private byte[] attachmentInvoice;
    private Map<String,byte[]> imagesMap;
}

