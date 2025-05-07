package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

public class KakaoOidcUser extends OAuth2ProviderUser {

    public KakaoOidcUser(ClientRegistration clientRegistration, Attributes attributes) {
        super(clientRegistration, attributes.getMainAttributes());
    }

    @Override
    public String getId() {
        return (String) getAttributes().get("sub");
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("nickname");
    }
}
