package site.tradelink.tradelink.cryptocurrency.dto;

import java.util.List;

/**
 * 호가창 SSE 이벤트 DTO (order-book 채널)
 *
 * 빗썸 orderbookdepth 기반
 * asks : 매도 호가 (index 0 = 최우선 = 가장 낮은 가격)
 * bids : 매수 호가 (index 0 = 최우선 = 가장 높은 가격)
 */
public record OrderBookDto(
        String ticker,
        List<OrderBookEntry> asks,
        List<OrderBookEntry> bids
) {
    /**
     * price    : 호가 (원)
     * quantity : 잔량 (암호화폐 단위, 소수점 가능)
     */
    public record OrderBookEntry(long price, double quantity) {}
}
