package site.tradelink.tradelink.cryptocurrency.dto;

import site.tradelink.tradelink.cryptocurrency.entity.TradeHistory;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;

import java.time.LocalDateTime;

/**
 * 체결내역 SSE 이벤트 DTO
 */
public record TradeHistoryDto(
        Long          seq,
        String        ticker,
        String        name,
        OrderSide side,
        Double        quantity,
        Long          execPrice,
        Double          totalAmount,
        LocalDateTime tradedAt
) {
    public static TradeHistoryDto from(TradeHistory e) {
        return new TradeHistoryDto(
                e.getSeq(),
                e.getTicker(),
                e.getName(),
                e.getSide(),
                e.getQuantity(),
                e.getExecPrice(),
                e.getTotalAmount(),
                e.getCreateTime()
        );
    }
}
