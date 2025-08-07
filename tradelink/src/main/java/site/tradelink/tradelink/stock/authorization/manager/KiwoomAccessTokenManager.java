package site.tradelink.tradelink.stock.authorization.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class KiwoomAccessTokenManager {

    private final OAuth2AuthorizedClientManager  authorizedClientManager;

    private final Map<String, OAuth2AccessToken> tokenCache = new ConcurrentHashMap<>();
    private static final String CLIENT_ID = "kiwoom";
    private final Object tokenLock = new Object();

    public String getAccessToken() {
        OAuth2AccessToken token = tokenCache.get(CLIENT_ID);

        if (isTokenValid(token)) {
            return token.getTokenValue();
        }

        synchronized (tokenLock) {
            token = tokenCache.get(CLIENT_ID);

            if (isTokenValid(token)) {
                return token.getTokenValue();
            }

            OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                    .withClientRegistrationId(CLIENT_ID)
                    .principal("kiwoom-client")
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(request);

            OAuth2AccessToken newToken = authorizedClient.getAccessToken();
            tokenCache.put(CLIENT_ID, newToken);

            return newToken.getTokenValue();
        }
    }

    private boolean isTokenValid(OAuth2AccessToken token) {
        return token != null && token.getExpiresAt() != null && Instant.now().isBefore(token.getExpiresAt().minusSeconds(300));
    }
}
