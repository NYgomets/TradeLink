package site.tradelink.tradelink.oauth2.common.converters;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;

public interface ProviderUserConverter<T1, T2, R> {
    R convert(T1 t1, T2 t2);
}
