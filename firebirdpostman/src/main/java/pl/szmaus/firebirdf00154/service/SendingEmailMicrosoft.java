package pl.szmaus.firebirdf00154.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;
import org.springframework.stereotype.Service;
import pl.szmaus.configuration.MailConfiguration;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class SendingEmailMicrosoft {
    private final MailConfiguration mailConfiguration;

    public SendingEmailMicrosoft(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }

    public void configurationMicrosoft365Email(String toEmail, String bccEmail, String htmlText, String subject, byte[] data, Map<String,byte[]> imagesMap) {

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(mailConfiguration.getClientId())
                .clientSecret(mailConfiguration.getClientSecret())
                .tenantId(mailConfiguration.getTenantId())
                .build();

        final TokenCredentialAuthProvider tokenCredAuthProvider =
                new TokenCredentialAuthProvider(Collections.singletonList("https://graph.microsoft.com/.default"), clientSecretCredential);

        final GraphServiceClient graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();

        Message message = new Message();
        message.subject = subject;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = htmlText ;
        message.body = body;

        List<Recipient> toRecipientsList = new LinkedList<Recipient>();
        toRecipientsList = Arrays.asList(toEmail.split(";",-1))
                .stream()
                .map(x-> {
                    Recipient recipient= new Recipient();
                    EmailAddress emailAddressRecipient = new EmailAddress();
                    emailAddressRecipient.address = x;
                    recipient.emailAddress = emailAddressRecipient;
                    return recipient;
                })
                .collect(Collectors.toList());

        message.toRecipients = toRecipientsList;

        List<Recipient> bccRecipientsList = new LinkedList<Recipient>();
        bccRecipientsList = Arrays.asList(bccEmail.split(";",-1))
                .stream()
                .map(x-> {
                    Recipient recipient= new Recipient();
                    EmailAddress emailAddressRecipient = new EmailAddress();
                    emailAddressRecipient.address = x;
                    recipient.emailAddress = emailAddressRecipient;
                    return recipient;
                })
               .collect(Collectors.toList());

        message.bccRecipients = bccRecipientsList;

        LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();
        if(data!=null ) {
            FileAttachment attachments = new FileAttachment();
            attachments.name = "Faktura.pdf";
            attachments.contentBytes = data;
            attachments.contentType = "inv/pdf";
            attachments.oDataType = "#microsoft.graph.fileAttachment";
            attachmentsList.add(attachments);
        }

        if(imagesMap!=null ) {
            for (Map.Entry<String,byte[]> entry : imagesMap.entrySet()) {
                FileAttachment attachments = new FileAttachment();
                attachments.name = "images.jpg";
                attachments.contentBytes = entry.getValue();
                attachments.contentType = "image/jpg";
                attachments.contentId = entry.getKey();
                attachments.isInline = true;
                attachments.oDataType = "#microsoft.graph.fileAttachment";
                attachmentsList.add(attachments);
            }
        }

            AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
            attachmentCollectionResponse.value = attachmentsList;
            AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
            message.attachments = attachmentCollectionPage;

        boolean saveToSentItems = true;

        graphClient
                .users(mailConfiguration.getFromEmail())
                .sendMail(
                        UserSendMailParameterSet
                                .newBuilder()
                                .withMessage(message)
                                .withSaveToSentItems(saveToSentItems)
                                .build())
                .buildRequest()
                .post();
    }
}
