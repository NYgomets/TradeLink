package site.tradelink.tradelink.stock.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

import java.time.LocalDate;

/**
 * 일별 최종 환율
 */
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_daily_currency_date", columnList = "currency_code, base_date")
        }
)
public class DailyExchangeRate extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dailyExchangeRate_seq")
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
    private LocalDate baseDate;

    /**
     * CurrentExchangeRate로부터 생성
     */
    public static DailyExchangeRate fromCurrent(CurrentExchangeRate current, LocalDate baseDate) {
        return DailyExchangeRate.builder()
                .currencyCode(current.getCurrencyCode())
                .currencyName(current.getCurrencyName())
                .rate(current.getRate())
                .changeAmount(current.getChangeAmount())
                .changePercent(current.getChangePercent())
                .baseDate(baseDate)
                .build();
    }

}
