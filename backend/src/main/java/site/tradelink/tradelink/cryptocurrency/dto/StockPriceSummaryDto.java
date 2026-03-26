package site.tradelink.tradelink.cryptocurrency.dto;

import java.time.LocalDateTime;

public record StockPriceSummaryDto(
    String        ticker,
    String        name,
    long          price,         // 현재가 (원)
    long          changeAmount,  // 전일 대비 변동액
    double        changePercent, // 전일 대비 변동률 (%)
    long          volume,        // 누적 거래량
    LocalDateTime updatedAt
) {}
