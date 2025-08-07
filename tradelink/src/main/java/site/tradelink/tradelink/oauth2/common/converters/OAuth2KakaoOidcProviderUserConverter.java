package site.tradelink.tradelink.oauth2.common.converters;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.tradelink.tradelink.oauth2.common.enums.SocialType;
import site.tradelink.tradelink.oauth2.common.util.OAuth2Utils;
import site.tradelink.tradelink.oauth2.dto.KakaoOidcUser;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;

public class OAuth2KakaoOidcProviderUserConverter implements ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> {

    @Override
    public ProviderUser convert(ClientRegistration clientRegistration, OAuth2User oAuth2User) {

        if (!clientRegistration.getRegistrationId().equals(SocialType.KAKAO.getSocialName())) {
            return null;
        }

        if (!(oAuth2User instanceof OidcUser)) {
            return null;
        }

        return new KakaoOidcUser(clientRegistration, OAuth2Utils.getMainAttributes(oAuth2User));
    }
}
