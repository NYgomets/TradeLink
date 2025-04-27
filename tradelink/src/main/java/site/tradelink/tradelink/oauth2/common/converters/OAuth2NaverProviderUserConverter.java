package site.tradelink.tradelink.oauth2.common.converters;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.tradelink.tradelink.oauth2.common.enums.SocialType;
import site.tradelink.tradelink.oauth2.dto.NaverUser;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;

public class OAuth2NaverProviderUserConverter implements ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> {
    @Override
    public ProviderUser convert(ClientRegistration clientRegistration, OAuth2User oAuth2User) {

        if (!clientRegistration.getRegistrationId().equals(SocialType.NAVER.getSocialName())) {
            return null;
        }

        return new NaverUser(clientRegistration, oAuth2User);
    }
}
