package site.tradelink.tradelink.cryptocurrency.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.StockPriceSummaryDto;

import java.time.LocalDateTime;

/**
 * 빗썸 ticker 메시지 처리
 *
 * - StockPriceCache 갱신 후 DirtyTracker.markDirty()만 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BithumbTickerHandler {

    private final StockPriceCache priceCache;
    private final DirtyTracker dirtyTracker;

    public void handle(JsonNode root) {
        try {
            JsonNode content = root.path("content");
            String symbol = content.path("symbol").asText(); // "BTC_KRW"
            String ticker = symbol.replace("_KRW", ""); // "BTC"

            long   price      = (long) Double.parseDouble(content.path("closePrice").asText());
            long   changeAmt  = (long) Double.parseDouble(content.path("chgAmt").asText());
            double changeRate = Double.parseDouble(content.path("chgRate").asText());
            long   volume     = (long) Double.parseDouble(content.path("volume").asText());

            StockPriceSummaryDto dto = new StockPriceSummaryDto(
                    ticker,
                    CryptoName.of(ticker),
                    price,
                    changeAmt,
                    changeRate,
                    volume,
                    LocalDateTime.now()
            );

            // 1. 캐시 갱신
            priceCache.putPrice(ticker, dto);

            // 2. dirty 표시
            dirtyTracker.markDirty(ticker);

        } catch (Exception e) {
            log.warn("[BithumbTicker] 처리 오류: {}", e.getMessage());
        }
    }
}
