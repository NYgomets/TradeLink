package site.tradelink.tradelink.stock.response;

import site.tradelink.tradelink.stock.entity.ExchangeRate;

import java.time.LocalDateTime;

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
}
