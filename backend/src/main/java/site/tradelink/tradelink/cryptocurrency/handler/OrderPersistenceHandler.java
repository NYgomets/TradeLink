package site.tradelink.tradelink.cryptocurrency.handler;

import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.disruptor.OrderEventModel;
import site.tradelink.tradelink.cryptocurrency.engine.StockTransactionService;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.enums.OrderStatus;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPersistenceHandler implements EventHandler<OrderEventModel> {

    private final OrderEventRepository orderEventRepository;
    private final StockTransactionService transactionService;
    private final WalletService walletService;
    private final SseEmitterManager sseManager;

    @Override
    public void onEvent(OrderEventModel event, long sequence, boolean endOfBatch) {

        try {
            if (event.getStatus() == OrderStatus.MATCHED) {
                processMatchedOrder(event);
            } else if (event.getStatus() == OrderStatus.FAILED) {
                processFailedOrder(event);
            }
        } catch (Exception e) {
            log.error("[PersistenceHandler] 영속화 및 후처리 중 치명적 예외 발생. memberSeq={}, ticker={}",
                    event.getMemberSeq(), event.getTicker(), e);
            // 여기서 발생한 예외는 로깅 후 보상(알림 등) 처리 수준으로 방어
            fallbackForCriticalError(event);
        } finally {
            // 링버퍼 회전 시 GC 발생을 막기 위해 모든 원시/참조 필드 초기화
            event.clear();
        }
    }

    private void processMatchedOrder(OrderEventModel event) {

        OrderEvent orderEvent = OrderEvent.create(
                event.getMemberSeq(),
                event.getTicker(),
                event.getSide(),
                event.getQuantity(),
                event.getReservedAmount()
        );

        OrderEvent savedEvent = orderEventRepository.save(orderEvent);

        transactionService.process(savedEvent, event.getExecutedPrice());

        try {
            sseManager.pushMyOrder(
                    event.getMemberSeq(),
                    event.getTicker(),
                    event.getExecutedPrice(),
                    event.getExecutedQuantity(),
                    event.getSide(),
                    "FILLED",
                    LocalDateTime.now()
            );
        } catch (Exception e) {
            log.warn("[PersistenceHandler] 체결 완료 SSE 알림 발송 실패: {}", e.getMessage());
        }

        event.setStatus(OrderStatus.COMPLETED);
    }

    private void processFailedOrder(OrderEventModel event) {
        // 매수 주문 실패 시, 사전에 묶어둔 예약금(reservedAmount)을 환불 처리
        if (OrderSide.BUY == event.getSide() && event.getReservedAmount() > 0) {
            try {
                walletService.cancelReservation(event.getMemberSeq(), event.getReservedAmount());
                log.info("[PersistenceHandler] 실패 주문 예약금 환불 완료. memberSeq={}, amount={}",
                        event.getMemberSeq(), event.getReservedAmount());
            } catch (Exception ex) {
                log.error("[PersistenceHandler] 실패 주문 복구(환불) 중 추가 예외 발생: {}", ex.getMessage());
            }
        }

        // 최종 실패 상태 알림 전송
        try {
            String errorMessage = event.getErrorCode() != null ? event.getErrorCode().getMessage() : "시스템 에러";
            sseManager.pushMyOrder(
                    event.getMemberSeq(),
                    event.getTicker(),
                    0L,
                    event.getQuantity(),
                    event.getSide(),
                    "FAILED: " + errorMessage,
                    LocalDateTime.now()
            );
        } catch (Exception ex) {
            log.warn("[PersistenceHandler] 실패 알림 발송 실패: {}", ex.getMessage());
        }
    }

    private void fallbackForCriticalError(OrderEventModel event) {
        // DB 다운 등 최악의 상황 시 최소한 클라이언트에게 실패 알림을 쏴주는 최후의 보루
        try {
            sseManager.pushMyOrder(
                    event.getMemberSeq(), event.getTicker(), 0L, event.getQuantity(), event.getSide(),
                    "SYSTEM_ERROR", LocalDateTime.now()
            );
        } catch (Exception ignored) {}
    }
}
