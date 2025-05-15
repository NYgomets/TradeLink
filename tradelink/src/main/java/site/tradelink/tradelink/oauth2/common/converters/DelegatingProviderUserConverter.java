package site.tradelink.tradelink.oauth2.common.converters;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class DelegatingProviderUserConverter implements ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> {

    private final List<ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser>> converters;

    public DelegatingProviderUserConverter() {
        List<ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser>> providerUserConverters = Arrays.asList(
                new OAuth2GoogleProviderUserConverter(),
                new OAuth2KakaoOidcProviderUserConverter(),
                new OAuth2KakaoProviderUserConverter(),
                new OAuth2NaverProviderUserConverter()
        );

        this.converters = Collections.unmodifiableList(providerUserConverters);
    }

    @Override
    public ProviderUser convert(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        for (ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> converter : converters) {
            ProviderUser providerUser = converter.convert(clientRegistration, oAuth2User);
            if (providerUser != null) {
                return providerUser;
            }
        }

        return null;
    }
}
