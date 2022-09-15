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
public class SendEmailMicrosoftImp implements SendEmailMicrosoft {
    private final MailConfiguration mailConfiguration;

    public SendEmailMicrosoftImp(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }

    public void configurationMicrosoft365Email(String toEmail, String bccEmail, String htmlText, String subject, byte[] data, Map<String,byte[]> imagesMap) {
        Message message = createMessage(toEmail, bccEmail, htmlText, subject);
        addAttachmentsToMessage(message, data,  imagesMap);
        configureGraphClient(message);
    }

    public void configurationMicrosoft365Email(String toEmail, String bccEmail, String htmlText, String subject){
        configurationMicrosoft365Email(toEmail, bccEmail, htmlText,  subject, null, null);
    }

    private Message createMessage(String toEmail, String bccEmail, String htmlText, String subject){
        Message message = new Message();
        message.subject = subject;
        ItemBody body = new ItemBody();
        body.contentType = BodyType.HTML;
        body.content = htmlText ;
        message.body = body;
        message.toRecipients = createRecipientsList(toEmail);
        message.bccRecipients = createRecipientsList(bccEmail);
        return message;
    }
    private  List<Recipient> createRecipientsList(String email) {
        return  Arrays.asList(email.split(";", -1))
                .stream()
                .map(x -> createRecipient(x))
                .collect(Collectors.toList());
    }

    private Recipient createRecipient(String email){
        Recipient recipient= new Recipient();
        EmailAddress emailAddressRecipient = new EmailAddress();
        emailAddressRecipient.address = email;
        recipient.emailAddress = emailAddressRecipient;
        return recipient;
    }

    private Message addAttachmentsToMessage(Message message, byte[] data, Map<String,byte[]> imagesMap){
        LinkedList<Attachment> attachmentsList = new LinkedList<Attachment>();
        if(data!=null ) {
            FileAttachment attachments = createFileAttachment("Faktura.pdf", data, "inv/pdf","#microsoft.graph.fileAttachment");
            attachmentsList.add(attachments);
        }

        if(imagesMap!=null ) {
            for (Map.Entry<String,byte[]> entry : imagesMap.entrySet()) {
                FileAttachment attachments = createFileAttachment("images.jpg", entry.getValue(), "image/jpg", "#microsoft.graph.fileAttachment");
                attachments.contentId = entry.getKey();
                attachments.isInline = true;
                attachmentsList.add(attachments);
            }
        }

        AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
        attachmentCollectionResponse.value = attachmentsList;
        AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
        message.attachments = attachmentCollectionPage;
        return  message;
    }

    private FileAttachment createFileAttachment(String attachmentName, byte[] attachmentData, String attachmentContent, String attachmentType){
        FileAttachment attachments = new FileAttachment();
        attachments.name = attachmentName;
        attachments.contentBytes = attachmentData;
        attachments.contentType = attachmentContent;
        attachments.oDataType = attachmentType;
        return attachments;
    }

    private void configureGraphClient(Message message){
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
