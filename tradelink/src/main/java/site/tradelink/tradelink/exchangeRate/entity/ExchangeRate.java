package site.tradelink.tradelink.exchangeRate.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 환율 기록 정보
 */
@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_history_currency_datetime", columnList = "currency_code, base_date_time")
        }
)
public class ExchangeRate extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchangeRate_seq")
    private Long seq;

    @Column
    private String currencyCode;

    @Column
    private Double rate;

    @Column
    private LocalDateTime baseDateTime;


}
