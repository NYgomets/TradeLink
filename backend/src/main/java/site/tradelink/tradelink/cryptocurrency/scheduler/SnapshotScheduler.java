package site.tradelink.tradelink.cryptocurrency.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.inMemory.DirtyTracker;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.inMemory.StockPriceCache;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;

import java.util.Set;

/**
 * 1초 스냅삿 스케줄러
 * dirty 종목만 골라서 SSE broadcast
 *
 * 현재가: StockPriceCache 전체
 * 호가: OrderBookCache.findTop5 (내부는 30호가 전체 보관. 전송 시에만 5단계로 자름
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotScheduler {

    private final DirtyTracker dirtyTracker;
    private final StockPriceCache priceCache;
    private final OrderBookCache orderBookCache;
    private final SseEmitterManager sseManager;

    @Scheduled(fixedRate = 1000)
    public void flush() {
        Set<String> tickers = dirtyTracker.getAndClear();
        if (tickers.isEmpty()) {
            return;
        }

        for (String ticker : tickers) {
            try {
                priceCache.findPrice(ticker)
                        .ifPresent(dto -> sseManager.broadcastPrice(ticker, dto));

                orderBookCache.findTop5(ticker)
                        .ifPresent(dto -> sseManager.broadcastOrderBook(ticker, dto));
            } catch (Exception e) {
                log.warn("[SnapshotScheduler] {} broadcast 실패: {}", ticker, e.getMessage());
            }
        }
    }
}
