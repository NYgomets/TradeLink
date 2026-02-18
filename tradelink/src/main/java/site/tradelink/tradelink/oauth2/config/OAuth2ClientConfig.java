package site.tradelink.tradelink.oauth2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import site.tradelink.tradelink.oauth2.common.mapper.CustomAuthorityMapper;
import site.tradelink.tradelink.oauth2.service.CustomOAuth2UserService;
import site.tradelink.tradelink.oauth2.service.CustomOidcUserService;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class OAuth2ClientConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/auth/**").access(new WebExpressionAuthorizationManager("hasAnyRole('USER')"))
                        .anyRequest().permitAll()
                );

        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                userInfoEndpointConfig -> userInfoEndpointConfig
                        .userService(customOAuth2UserService)
                        .oidcUserService(customOidcUserService)
                    )
                .authorizationEndpoint(
                        authorizationEndpointConfig -> authorizationEndpointConfig
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                )
        );

        http.logout(logout -> logout
                .logoutSuccessUrl("/exchange-rates")
        );

        return http.build();
    }
}
