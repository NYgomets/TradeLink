package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public class NaverUser extends OAuth2ProviderUser {


    public NaverUser(ClientRegistration clientRegistration, Attributes attributes) {
        super(clientRegistration, attributes.getSubAttributes());
    }

    @Override
    public String getId() {
        return (String)getAttributes().get("id");
    }

    @Override
    public String getUsername() {
        return (String) getAttributes().get("nickname");
    }
}
