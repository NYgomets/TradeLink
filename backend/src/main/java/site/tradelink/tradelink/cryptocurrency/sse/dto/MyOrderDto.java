package site.tradelink.tradelink.cryptocurrency.sse.dto;

import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;

import java.time.LocalDateTime;

public record MyOrderDto (
        String ticker,
        long price,
        double quantity,
        OrderSide side,
        String status,
        LocalDateTime at
) {}
