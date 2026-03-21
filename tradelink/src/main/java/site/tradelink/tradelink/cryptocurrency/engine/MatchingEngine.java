package site.tradelink.tradelink.cryptocurrency.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
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
    private final WalletService walletService;
    private final SseEmitterManager sseManger;

    public void execute(OrderEvent event) {
        String ticker = event.getTicker();
        OrderSide side = event.getSide();
        Double quantity = event.getQuantity();
        Long memberSeq = event.getMemberSeq();

        // 1. 체결가 결정 (캐시 조회, 트랜잭션 불필요)
        long execPrice = orderBookCache.getBestPrice(ticker, side)
                .orElseThrow(() -> new IllegalStateException(
                        "호가 없음 또는 stale: " + ticker));

        LocalDateTime now = LocalDateTime.now();

        // 2. DB 작업 (트랜잭션 범위 최소화)
        try {
            transactionService.process(event, execPrice);
        } catch (IllegalStateException e) {
            log.warn("[Matching] 체결 실패 memberSeq={} ticker={}: {}",
                    memberSeq, ticker, e.getMessage());

            // 슬리피지 실패: 트랜잭션 밖에서 예약금 환불
            // (트랜잭션 안에서 환불 시 예외로 인해 환불도 롤백되는 문제 방지)
            if (OrderSide.BUY.equals(event.getSide()) && event.getReservedPrice() > 0) {
                try {
                    walletService.cancelReservation(memberSeq, event.getReservedPrice());
                } catch (Exception ex) {
                    log.error("[Matching] 예약금 환불 실패 memberSeq={}: {}", memberSeq, ex.getMessage());
                }
            }

            try {
                sseManger.pushMyOrder(memberSeq, ticker, execPrice, quantity, side, "FAILED", now);
            } catch (Exception ex) {
                log.warn("[Matching] SSE 실패 알림 전송 실패: {}", ex.getMessage());
            }
            throw e;
        }

        // 3. SSE push (실패해도 클라이언트가 GET /orders로 폴백 가능)
        try {
            sseManger.pushMyOrder(memberSeq, ticker, execPrice, quantity, side, "FILLED", now);
        } catch (Exception e) {
            log.warn("[Matching] SSE push 실패 memberId={}: {}", memberSeq, e.getMessage());
        }
    }
}
