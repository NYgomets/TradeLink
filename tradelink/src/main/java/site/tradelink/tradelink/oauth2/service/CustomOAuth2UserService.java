package site.tradelink.tradelink.oauth2.service;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;

@Service
public class CustomOAuth2UserService extends AbstractOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    public CustomOAuth2UserService(MemberService memberService, MemberRepository memberRepository) {
        super(memberService, memberRepository);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        ClientRegistration clientRegistration = userRequest.getClientRegistration();
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        // 현재 진행중인 서비스를 구분하여 ProviderUser 객체 생성
        ProviderUser providerUser = super.providerUser(clientRegistration, oAuth2User);

        // 회원가입하기
        super.register(providerUser);

        return oAuth2User;
    }
}
