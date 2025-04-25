package site.tradelink.tradelink.oauth2.service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.oauth2.dto.GoogleUser;
import site.tradelink.tradelink.oauth2.dto.KakaoUser;
import site.tradelink.tradelink.oauth2.dto.NaverUser;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;

import java.util.Optional;

@Service
public abstract class AbstractOAuth2UserService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public AbstractOAuth2UserService(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    protected ProviderUser providerUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        String registrationId = clientRegistration.getRegistrationId();

        if (registrationId.equals("google")) {
            return new GoogleUser(clientRegistration, oAuth2User);
        } else if (registrationId.equals("kakao")) {
            return new KakaoUser(clientRegistration, oAuth2User);
        } else if (registrationId.equals("naver")) {
            return new NaverUser(clientRegistration, oAuth2User);
        }

        return null;
    }

    protected void register(ProviderUser providerUser) {
        Optional<Member> member = memberRepository.findByMemberId(providerUser.getId());

        if(member.isEmpty()) {
            memberService.register(providerUser);
        }
    }
}
