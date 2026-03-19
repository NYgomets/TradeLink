package site.tradelink.tradelink.cryptocurrency.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;

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

            // 현재 캐시 호가 가져오기 (snapshot이 없으면 버림)
            OrderBookDto current = orderBookCache.find(ticker).orElse(null);

            if (current == null) {
                log.debug("[OrderBook] {} 스냅샷이 아직 없어 변경분을 무시합니다.", ticker);
                return; // Snapshot이 올 때까지 기다림
            }

            List<OrderBookDto.OrderBookEntry> asks = new ArrayList<>(current.asks());
            List<OrderBookDto.OrderBookEntry> bids = new ArrayList<>(current.bids());

            // 변경분 적용
            for (JsonNode item : list) {
                String orderType = item.path("orderType").asText(); // "ask" | "bid"
                long   price     = (long) Double.parseDouble(item.path("price").asText());
                double quantity  = Double.parseDouble(item.path("quantity").asText());

                List<OrderBookDto.OrderBookEntry> target = "ask".equals(orderType) ? asks : bids;
                applyUpdate(target, price, quantity);
            }

            // 재정렬 + 상위 5단계만 유지
            asks.sort((a, b) -> Long.compare(a.price(), b.price()));  // 오름차순
            bids.sort((a, b) -> Long.compare(b.price(), a.price()));  // 내림차순

            List<OrderBookDto.OrderBookEntry> trimmedAsks = asks.size() > DEPTH ? asks.subList(0, DEPTH) : asks;
            List<OrderBookDto.OrderBookEntry> trimmedBids = bids.size() > DEPTH ? bids.subList(0, DEPTH) : bids;

            OrderBookDto updated = new OrderBookDto(ticker, trimmedAsks, trimmedBids);

            if (!updated.equals(current)) {
                orderBookCache.put(ticker, updated);
                dirtyTracker.markDirty(ticker);
            }
        } catch (Exception e) {
            log.warn("[OrderBookDepth] 처리 오류: {}", e.getMessage());
        }
    }

    /**
     * 단일 호가 레벨 업데이트
     * quantity == 0 → 해당 가격 제거
     * quantity  > 0 → 해당 가격 잔량 갱신 (없으면 추가)
     */
    private void applyUpdate(List<OrderBookDto.OrderBookEntry> entries, long price, double quantity) {
        entries.removeIf(e -> e.price() == price); // 기존 레벨 제거

        if (quantity > 0) {
            entries.add(new OrderBookDto.OrderBookEntry(price, quantity)); // 새 잔량으로 추가
        }
        // quantity == 0 이면 제거만 하고 끝
    }
}
