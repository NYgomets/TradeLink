package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

public class KakaoUser extends OAuth2ProviderUser {

    private final Map<String, Object> otherAttributes;

    public KakaoUser(ClientRegistration clientRegistration, Attributes attributes) {
        super(clientRegistration, attributes.getSubAttributes());
        this.otherAttributes = attributes.getOtherAttributes();
    }

    @Override
    public String getId() {
        return (String)getAttributes().get("id");
    }

    @Override
    public String getUsername() {
        return (String) otherAttributes.get("nickname");
    }
}
