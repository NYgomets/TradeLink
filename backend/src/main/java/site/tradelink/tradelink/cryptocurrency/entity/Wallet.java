package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

/**
 * 모의투자 지감
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = @Index(name = "idx_wallet_member", columnList = "memberSeq, availableBalance")
)
public class Wallet extends BaseEntity {

    private static final long INITIAL_BALANCE = 200_000_000L; // 초기 시드머니 2억

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private Long memberSeq;

    private Long balance; // 총 잔액

    private Long availableBalance;  // 주문 가능 금액

    public static Wallet create(Long memberSeq) {
        return Wallet.builder()
                .memberSeq(memberSeq)
                .balance(INITIAL_BALANCE)
                .availableBalance(INITIAL_BALANCE)
                .build();
    }
}
