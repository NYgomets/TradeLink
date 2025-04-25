package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NaverUser extends OAuth2ProviderUser {
    public NaverUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        super(clientRegistration, (ConcurrentHashMap<String, Object>) oAuth2User.getAttributes());
    }

    @Override
    public String getId() {
        return (String)getAttributes().get("id");
    }
}
