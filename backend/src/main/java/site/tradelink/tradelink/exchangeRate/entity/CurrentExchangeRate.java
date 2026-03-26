package site.tradelink.tradelink.exchangeRate.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

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

    /**
     * 환율 업데이트 및 변동폭 계산
     */
    public void updateRate(Double newRate, Double previousRate, LocalDateTime newDateTime) {
        this.rate = newRate;
        this.baseDateTime = newDateTime;
        calculateChange(previousRate);
    }

    /**
     * 변동폭/변동률 계산
     */
    private void calculateChange(Double previousRate) {
        if (previousRate == null || previousRate <= 0) {
            this.changePercent = 0.0;
            this.changeAmount = 0.0;
            return;
        }

        this.changeAmount = this.rate - previousRate;
        this.changePercent = (this.changeAmount / previousRate) * 100;
    }
}
