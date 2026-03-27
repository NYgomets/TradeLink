package site.tradelink.tradelink.exchangeRate.entity;

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
}
