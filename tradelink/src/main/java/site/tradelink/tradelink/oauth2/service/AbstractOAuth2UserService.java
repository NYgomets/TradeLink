package site.tradelink.tradelink.oauth2.service;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.oauth2.common.converters.ProviderUserConverter;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;

import java.util.Optional;

@Service
public abstract class AbstractOAuth2UserService {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> providerUserConverter;

    public AbstractOAuth2UserService(MemberService memberService, MemberRepository memberRepository, ProviderUserConverter<ClientRegistration, OAuth2User, ProviderUser> providerUserConverter) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.providerUserConverter = providerUserConverter;
    }

    // coverter를 사용하여 ProviderUser 객체 반환
    protected ProviderUser providerUser(ClientRegistration clientRegistration, OAuth2User oAuth2User) {
        return providerUserConverter.convert(clientRegistration, oAuth2User);
    }

    protected void register(ProviderUser providerUser) {
        Optional<Member> member = memberRepository.findByMemberId(providerUser.getId());

        if(member.isEmpty()) {
            memberService.register(providerUser);
        }
    }
}
