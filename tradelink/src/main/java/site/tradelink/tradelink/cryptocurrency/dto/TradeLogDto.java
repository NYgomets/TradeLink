package site.tradelink.tradelink.cryptocurrency.dto;

import java.time.LocalDateTime;

/**
 * 체결내역 SSE 이벤트 DTO
 */
public record TradeLogDto (
        String        ticker,
        long          price,        // 체결가
        long          quantity,     // 체결량
        String        side,         // "BUY" | "SELL"
        LocalDateTime tradedAt
) {}
