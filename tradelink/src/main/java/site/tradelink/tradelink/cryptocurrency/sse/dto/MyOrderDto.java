package site.tradelink.tradelink.cryptocurrency.sse.dto;

import java.time.LocalDateTime;

public record MyOrderDto (
        String        ticker,
        long          price,
        double          quantity,
        String        side,
        String        status,
        LocalDateTime at
) {}
