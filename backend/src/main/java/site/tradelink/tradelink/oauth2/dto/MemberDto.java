package site.tradelink.tradelink.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import site.tradelink.tradelink.cryptocurrency.entity.Wallet;
import site.tradelink.tradelink.oauth2.entity.Member;

@Getter
@Builder
public class MemberDto {
    private Long memberSeq;
    private String memberName;
    private String email;
    private String provider;
    private long balance;           // 총 잔고
    private long availableBalance;  // 사용 가능 잔고

    public static MemberDto from(Member member, Wallet wallet) {
        return MemberDto.builder()
                .memberSeq(member.getSeq())
                .memberName(member.getMemberName())
                .email(member.getEmail())
                .provider(member.getProvider())
                .balance(wallet.getBalance())
                .availableBalance(wallet.getAvailableBalance())
                .build();
    }
}
