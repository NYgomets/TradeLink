package site.tradelink.tradelink.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public Member register(ProviderUser providerUser) {
        Member member = Member.builder()
                .memberId(providerUser.getId())
                .memberName(providerUser.getUsername())
                .provider(providerUser.getProvider())
                .email(providerUser.getEmail())
                .build();

        return memberRepository.save(member);
    }
}
