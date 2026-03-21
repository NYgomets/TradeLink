package site.tradelink.tradelink.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;
import site.tradelink.tradelink.cryptocurrency.repository.WalletRepository;
import site.tradelink.tradelink.oauth2.dto.ProviderUser;
import site.tradelink.tradelink.oauth2.entity.Member;
import site.tradelink.tradelink.oauth2.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    /**
     * 신규 회원 등록 Member 저장 후 Wallet 자동 생성 (초기 시드머니 2억) 같은 트랜잭션 안에서 처리
     */
    public Member register(ProviderUser providerUser) {
        Member member = Member.builder()
                .memberId(providerUser.getId())
                .memberName(providerUser.getUsername())
                .provider(providerUser.getProvider())
                .email(providerUser.getEmail())
                .build();

        Member saved = memberRepository.save(member);

        walletRepository.save(Wallet.create(saved.getSeq()));

        return saved;
    }
}
