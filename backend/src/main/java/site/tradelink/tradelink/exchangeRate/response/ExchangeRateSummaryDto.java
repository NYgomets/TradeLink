package site.tradelink.tradelink.exchangeRate.response;

import site.tradelink.tradelink.exchangeRate.entity.CurrentExchangeRate;
import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;

import java.time.LocalDateTime;

public record ExchangeRateSummaryDto(

    String currencyCode,
    String currencyName,
    Double rate,
    Double changeAmount,
    Double changePercent,
    LocalDateTime baseDateTime // Daily의 경우 LocalDate.atTime(23, 59, 59)
) {
    public static ExchangeRateSummaryDto fromCurrent(CurrentExchangeRate currentExchangeRate) {
        return new ExchangeRateSummaryDto(
                currentExchangeRate.getCurrencyCode(),
                currentExchangeRate.getCurrencyName(),
                currentExchangeRate.getRate(),
                currentExchangeRate.getChangeAmount(),
                currentExchangeRate.getChangePercent(),
                currentExchangeRate.getBaseDateTime()
        );
    }

    public static ExchangeRateSummaryDto fromDaily(DailyExchangeRate dailyExchangeRate) {
        return new ExchangeRateSummaryDto(
                dailyExchangeRate.getCurrencyCode(),
                dailyExchangeRate.getCurrencyName(),
                dailyExchangeRate.getRate(),
                dailyExchangeRate.getChangeAmount(),
                dailyExchangeRate.getChangePercent(),
                dailyExchangeRate.getBaseDate().atTime(23, 59, 59)
        );
    }
}
