package site.tradelink.tradelink.exchangeRate.response;

import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;
import site.tradelink.tradelink.exchangeRate.entity.ExchangeRate;

import java.time.LocalDateTime;

/**
 * 차트용 공통 DTO
 * - 1일 이하: ExchangeRate (분 단위)
 * - 1일 초과: DailyExchangeRate (일 단위)
 * 두 경우 모두 이 DTO로 통일
 */
public record ExchangeRateChartPointDto(
        Double rate,
        LocalDateTime baseDateTime
) {
    public static ExchangeRateChartPointDto fromHistory(ExchangeRate exchangeRate) {
        return new ExchangeRateChartPointDto(
                exchangeRate.getRate(),
                exchangeRate.getBaseDateTime()
        );
    }

    public static ExchangeRateChartPointDto fromDaily(DailyExchangeRate dailyExchangeRate) {
        return new ExchangeRateChartPointDto(
                dailyExchangeRate.getRate(),
                dailyExchangeRate.getBaseDate().atTime(23, 30)
        );
    }
}
