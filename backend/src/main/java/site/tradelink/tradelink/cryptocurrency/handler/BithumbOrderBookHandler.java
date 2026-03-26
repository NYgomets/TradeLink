package site.tradelink.tradelink.cryptocurrency.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;
import site.tradelink.tradelink.cryptocurrency.inMemory.DirtyTracker;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;

import java.util.ArrayList;
import java.util.List;

/**
 * 빗썸 orderbookdepth 메시지 처리 (변경분 업데이트)
 *
 * 전제: 연결 직후 orderbooksnapshot으로 초기 호가가 이미 캐시에 있음
 * -> orderbookdepth는 변경된 호가 레벨만 전송
 *
 * 실제 응답 구조:
 *  {
 *    "type": "orderbookdepth",
 *    "content": {
 *      "list": [
 *        { "symbol": "BTC_KRW", "orderType": "ask", "price": "10593000", "quantity": "1.11", "total": "3" },
 *        { "symbol": "BTC_KRW", "orderType": "bid", "price": "10532000", "quantity": "0",    "total": "0" }
 *      ],
 *      "datetime": 1580268255864325
 *    }
 *  }
 *
 * quantity = "0" → 해당 호가 레벨 제거
 * quantity > 0   → 해당 가격의 잔량 갱신 (없으면 추가, 있으면 교체)
 *
 * 처리 후 asks 오름차순 / bids 내림차순 재정렬 → 상위 5단계만 유지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BithumbOrderBookHandler {

    private static final int DEPTH = 5;

    private final OrderBookCache orderBookCache;
    private final DirtyTracker dirtyTracker;

    public void handle(JsonNode root) {
        try {
            JsonNode list = root.path("content").path("list");
            if (!list.isArray() || list.isEmpty()) {
                return;
            }

            String ticker = list.get(0).path("symbol").asText().replace("_KRW", "");

            // snapshot 없으면 명시적으로 무시
            if (orderBookCache.find(ticker).isEmpty()) {
                log.debug("[OrderBookDepth] {} 스냅샷 미수신, 변경분 무시", ticker);
                return;
            }

            List<OrderBookCache.DepthUpdate> updates = new ArrayList<>();
            for (JsonNode item : list) {
                updates.add(new OrderBookCache.DepthUpdate(
                        item.path("orderType").asText(),
                        (long) Double.parseDouble(item.path("price").asText()),
                        Double.parseDouble(item.path("quantity").asText())
                ));
            }

            // 실제 변경 발생 시에만 dirty 표시 -> 불필요한 SSE broadcast 방지
            boolean changed = orderBookCache.merge(ticker, updates);
            if (changed) {
                dirtyTracker.markDirty(ticker);
            }

        } catch (Exception e) {
            log.warn("[OrderBookDepth] 처리 오류: {}", e.getMessage());
        }
    }
}
