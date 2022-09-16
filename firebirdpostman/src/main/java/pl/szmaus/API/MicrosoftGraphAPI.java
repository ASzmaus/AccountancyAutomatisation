package pl.szmaus.API;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonElement;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.szmaus.configuration.ImportConfiguration;
import pl.szmaus.configuration.MailConfiguration;

import java.io.InputStream;
import java.util.Collections;

@Log4j2
@Service
public class MicrosoftGraphAPI {
    private final MailConfiguration mailConfiguration;
    private final ImportConfiguration importConfiguration;

    public MicrosoftGraphAPI(MailConfiguration mailConfiguration, ImportConfiguration importConfiguration) {
        this.mailConfiguration = mailConfiguration;
        this.importConfiguration = importConfiguration;
    }

    public ClientSecretCredential createClientSecretCredential(){
        return  new ClientSecretCredentialBuilder()
                .clientId(mailConfiguration.getClientId())
                .clientSecret(mailConfiguration.getClientSecret())
                .tenantId(mailConfiguration.getTenantId())
                .build();
    }

    public TokenCredentialAuthProvider createTokenCredentialAuthProvider(ClientSecretCredential clientSecretCredential ){
        return new TokenCredentialAuthProvider(Collections.singletonList("https://graph.microsoft.com/.default"), clientSecretCredential);
    }

    public GraphServiceClient createGraphClient( TokenCredentialAuthProvider tokenCredAuthProvider){
        return  GraphServiceClient
                .builder()
                .authenticationProvider(tokenCredAuthProvider)
                .buildClient();
    }

    public InputStream getMyFile(String completePathOfFile) {
            ClientSecretCredential clientSecretCredential =  createClientSecretCredential();
            TokenCredentialAuthProvider tokenCredentialAuthProvider = createTokenCredentialAuthProvider(clientSecretCredential);
            GraphServiceClient graphClient =  createGraphClient(tokenCredentialAuthProvider);
            String pathToResourceSite = importConfiguration.getPathToResourceSite();
            String mainSiteId = ((JsonElement) graphClient.customRequest(pathToResourceSite)
                    .buildRequest()
                    .get())
                    .getAsJsonObject()
                    .get("id")
                    .getAsString();

            String siteId = mainSiteId.substring(mainSiteId.indexOf(",") + 1,mainSiteId.lastIndexOf(","));
            InputStream driveItem = graphClient.sites()
                    .byId(siteId).drive().root()
                    .itemWithPath(completePathOfFile)
                    .content().buildRequest().get();
            return driveItem;
    }
}
