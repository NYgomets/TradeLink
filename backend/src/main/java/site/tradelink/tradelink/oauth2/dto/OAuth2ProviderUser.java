package site.tradelink.tradelink.oauth2.dto;

import lombok.Getter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

@Getter
public abstract class OAuth2ProviderUser implements ProviderUser{

    private ClientRegistration clientRegistration;
    // ProviderUserConverter 객체가 호출될 때마다 새로운 attributes 객체를 생성하고 있기에
    // ConcurrentHashMap에서 HashMap으로 바꿈.
    private Map<String, Object> attributes;

    public OAuth2ProviderUser(ClientRegistration clientRegistration, Map<String, Object> attributes) {
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
}
