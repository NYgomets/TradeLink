package site.tradelink.tradelink.oauth2.dto;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

public class KakaoUser extends OAuth2ProviderUser {

    private final Map<String, Object> mainAttributes;
    private final Map<String, Object> otherAttributes;

    public KakaoUser(ClientRegistration clientRegistration, Attributes attributes) {
        super(clientRegistration, attributes.getSubAttributes());
        this.mainAttributes = attributes.getMainAttributes();
        this.otherAttributes = attributes.getOtherAttributes();
    }

    @Override
    public String getId() {
        return String.valueOf(mainAttributes.get("id"));
    }

    @Override
    public String getUsername() {
        return (String) otherAttributes.get("nickname");
    }
}
