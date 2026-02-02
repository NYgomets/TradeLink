package site.tradelink.tradelink.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 최신 환율 정보 엔티티
 */
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrentExchangeRate extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "currentExchangeRate_seq")
    private Long seq;

    @Column
    private String currencyCode;

    @Column
    private String currencyName;

    @Column
    private Double rate;

    @Column
    private Double changeAmount;

    @Column
    private Double changePercent;

    @Column
    private LocalDateTime baseDateTime;

    // 변동폭/변동률 계산 메서드
    public void calculateChange(Double previousRate) {
        if (previousRate != null && previousRate > 0) {
            this.changeAmount = this.rate - previousRate;
            this.changePercent = (this.changeAmount/previousRate) * 100;
        }
    }
}
