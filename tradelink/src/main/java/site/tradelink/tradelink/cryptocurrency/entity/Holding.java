package site.tradelink.tradelink.cryptocurrency.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

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

    public void buy(long price, long qty) {
        double totalCost = this.avgPrice * this.quantity + price * qty;
        this.quantity += qty;
        this.avgPrice  = totalCost / this.quantity;
    }

    public void sell(long qty) {
        if (qty > this.quantity) {
            throw new IllegalStateException(
                    "보유 수량 부족: " + this.ticker
                            + " (보유=" + this.quantity + ", 요청=" + qty + ")");
        }
        this.quantity -= qty;
    }
}
