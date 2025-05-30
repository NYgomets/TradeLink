package site.tradelink.tradelink.oauth2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import site.tradelink.tradelink.oauth2.common.mapper.CustomAuthorityMapper;
import site.tradelink.tradelink.oauth2.common.resolver.CustomOAuth2AuthorizationRequestResolver;

@Configuration
@RequiredArgsConstructor
public class OAuth2AppConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public GrantedAuthoritiesMapper customAuthorityMapper() {
        return new CustomAuthorityMapper();
    }

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver() {
        return new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }
}
