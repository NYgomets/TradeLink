package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.supports.enums.ErrorCode;
import site.tradelink.tradelink.supports.exception.CustomException;

/**
 * 보유 주식
 */
@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uq_holding_member_ticker",
                columnNames = {"memberSeq", "ticker"}
        )
)
public class Holding extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    private Long memberSeq;

    private String ticker;

    private String name;

    private Double quantity;

    // 평균 매수가
    private Double avgPrice;

    public void buy(long price, double qty) {
        double totalCost = this.avgPrice * this.quantity + price * qty;
        this.quantity += qty;
        this.avgPrice  = totalCost / this.quantity;
    }

    public void sell(double qty) {
        if (qty > this.quantity) {
            throw new CustomException(ErrorCode.INSUFFICIENT_HOLDING);
        }
        this.quantity -= qty;
    }

    public static Holding create(Long memberSeq, String ticker, String name) {
        return Holding.builder()
                .memberSeq(memberSeq)
                .ticker(ticker)
                .name(name)
                .quantity(0.0)
                .avgPrice(0.0)
                .build();
    }
}
