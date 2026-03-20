package site.tradelink.tradelink.cryptocurrency.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.inMemory.DirtyTracker;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;

import java.time.LocalDateTime;

/**
 * 시장가 체결 엔진
 * 체결가: OrderBookCache 최우선 호가 기준
 * SSE: my-order -> 해당 멤버에게만 즉시 push (체결 알림)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private final OrderBookCache orderBookCache;
    private final StockTransactionService transactionService;
    private final SseEmitterManager sseManger;
    private final DirtyTracker dirtyTracker;

    public void execute(OrderEvent event) {
        String ticker = event.getTicker();
        String side = event.getSide();
        Double quantity = event.getQuantity();
        Long memberSeq = event.getMemberSeq();

        // 1. 체결가 결정 (캐시 조회, 트랜잭션 불필요)
        long execPrice = orderBookCache.getBestPrice(ticker, side)
                .orElseThrow(() -> new IllegalStateException(
                        "호가 없음 또는 stale: " + ticker));

        LocalDateTime now = LocalDateTime.now();

        // 2. DB 작업 (트랜잭션 범위 최소화)
        transactionService.process(event, execPrice);

        // 3. 캐시 차감 + dirty 표시  (실패해도 빗썸 다음 수신 시 자연 복구)
        try {
            orderBookCache.consume(ticker, side, execPrice, quantity);
            dirtyTracker.markDirty(ticker);
        } catch (Exception e) {
            log.warn("[Matching] 캐시 차감 실패 (자연 복구됨) ticker={}: {}", ticker, e.getMessage());
        }

        // 4. SSE push (실패해도 클라이언트가 GET /orders로 폴백 가능)
        try {
            sseManger.pushMyOrder(memberSeq, ticker, execPrice, quantity, side, "FILLED", now);
        } catch (Exception e) {
            log.warn("[Matching] SSE push 실패 memberId={}: {}", memberSeq, e.getMessage());
        }
    }
}
