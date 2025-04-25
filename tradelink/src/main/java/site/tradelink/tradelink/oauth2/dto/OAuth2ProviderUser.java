package site.tradelink.tradelink.oauth2.dto;

import lombok.Getter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class OAuth2ProviderUser implements ProviderUser{
    private ClientRegistration clientRegistration;
    private ConcurrentHashMap<String, Object> attributes;

    public OAuth2ProviderUser(ClientRegistration clientRegistration, ConcurrentHashMap<String, Object> attributes) {
        this.clientRegistration = clientRegistration;
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getProvider() {
        return clientRegistration.getRegistrationId();
    }

    @Override
    public String getUsername() {
        return getEmail().substring(0, getEmail().indexOf("@"));
    }
}
