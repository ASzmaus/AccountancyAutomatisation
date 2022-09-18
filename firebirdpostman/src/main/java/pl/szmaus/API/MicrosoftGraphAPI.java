package pl.szmaus.API;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonElement;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ItemReference;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.szmaus.configuration.MailConfiguration;
import java.io.InputStream;
import java.util.Collections;
import static java.time.LocalDateTime.now;


@Log4j2
@Service
public class MicrosoftGraphAPI{
    private final MailConfiguration mailConfiguration;

    public MicrosoftGraphAPI(MailConfiguration mailConfiguration) {
        this.mailConfiguration = mailConfiguration;
    }

    public ClientSecretCredential createClientSecretCredential() {
        return new ClientSecretCredentialBuilder()
                .clientId(mailConfiguration.getClientId())
                .clientSecret(mailConfiguration.getClientSecret())
                .tenantId(mailConfiguration.getTenantId())
                .build();
    }

    public TokenCredentialAuthProvider createTokenCredentialAuthProvider(ClientSecretCredential clientSecretCredential) {
        return new TokenCredentialAuthProvider(Collections.singletonList("https://graph.microsoft.com/.default"), clientSecretCredential);
    }

    public GraphServiceClient createGraphClient(TokenCredentialAuthProvider tokenCredAuthProvider) {
        return GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();
    }

    public  GraphServiceClient returnGraphServiceClient() {
        ClientSecretCredential clientSecretCredential = createClientSecretCredential();
        TokenCredentialAuthProvider tokenCredentialAuthProvider = createTokenCredentialAuthProvider(clientSecretCredential);
        return createGraphClient(tokenCredentialAuthProvider);
    }

    private String getSiteId(String pathToResourceSite, GraphServiceClient graphClient) {
        String mainSiteId = ((JsonElement) graphClient.customRequest(pathToResourceSite)
                .buildRequest()
                .get())
                .getAsJsonObject()
                .get("id")
                .getAsString();
        return mainSiteId.substring(mainSiteId.indexOf(",") + 1, mainSiteId.lastIndexOf(","));
    }

    private String getItemId(GraphServiceClient graphClient, String siteId, String completePathOfFile) {
        return graphClient.sites()
                .byId(siteId).drive().root()
                .itemWithPath(completePathOfFile)
                .buildRequest().get().id;
    }

    public InputStream getContentMyFile(String pathToResourceSite, String completePathOfFile) throws GraphServiceException {

        GraphServiceClient graphClient = returnGraphServiceClient();
        String siteId = getSiteId(pathToResourceSite, graphClient);

        return graphClient.sites()
                .byId(siteId).drive().root()
                .itemWithPath(completePathOfFile)
                .content().buildRequest().get();
    }

    public void moveMyFile(String completePathOfImportedFile, String completePathOfNewFolder, String pathToResourceSite) throws GraphServiceException{
        GraphServiceClient graphClient = returnGraphServiceClient();
        String siteId = getSiteId(pathToResourceSite, graphClient);
        String newFolderId = getItemId(graphClient, siteId, completePathOfNewFolder);

        DriveItem driveItem = new DriveItem();
        ItemReference parentReference = new ItemReference();
        parentReference.id = newFolderId;
        driveItem.parentReference = parentReference;
        driveItem.name = "import"+now().toString().replace(":","-") + ".csv";

        graphClient.sites()
                .byId(siteId).drive().root()
                .itemWithPath(completePathOfImportedFile)
                .buildRequest()
                .patch(driveItem);
    }
}
