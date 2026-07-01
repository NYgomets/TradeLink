package site.tradelink.tradelink.cryptocurrency.handler;

import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.disruptor.OrderEventModel;
import site.tradelink.tradelink.cryptocurrency.enums.OrderStatus;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.supports.enums.ErrorCode;
import site.tradelink.tradelink.supports.exception.CustomException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMatchingHandler implements EventHandler<OrderEventModel> {

    private final OrderBookCache orderBookCache;

    @Override
    public void onEvent(OrderEventModel event, long sequence, boolean endOfBatch) {
        // PENDING 상태인 주문만 처리
        if (event.getStatus() != OrderStatus.PENDING) {
            return;
        }

        try {
            // 인메모리 호가 캐시에서 최적가 조회
            long bestPrice = orderBookCache.getBestPrice(event.getTicker(), event.getSide())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDERBOOK_STALE));

            event.setExecutedPrice(bestPrice);
            event.setExecutedQuantity(event.getQuantity());
            event.setStatus(OrderStatus.MATCHED);
        } catch (CustomException e) {
            // 프로젝트 커스텀 예외 발생 시 내부 ErrorCode 매핑 후 FAILED 전환
            log.warn("[MatchingHandler] 호가 매칭 실패 - memberSeq: {}, ticker: {}, 사유: {}",
                    event.getMemberSeq(), event.getTicker(), e.getErrorCode().getMessage());

            event.setErrorCode(e.getErrorCode());
            event.setStatus(OrderStatus.FAILED);
        }
    }
}
