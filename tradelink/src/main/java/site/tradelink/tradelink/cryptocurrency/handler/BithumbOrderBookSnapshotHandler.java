package site.tradelink.tradelink.cryptocurrency.handler;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;

import java.util.ArrayList;
import java.util.List;

/**
 * 빗썸 orderbooksnapshot 메시지 처리
 *
 * 웹소켓 연결 직후 전체 호가 초기화 이후 변경분은 BitumbOrderBookHandler가 처리
 *
 * 실제 응답 구조:
 * {
 *   "type": "orderbooksnapshot",
 *   "content": {
 *     "symbol": "BTC_KRW",
 *     "asks": [["37458000","0.9986"], ["37461000","0.0487"]], // [가격, 잔량] 배열
 *     "bids": [["37452000","0.0115"], ["37450000","0.0614"]]
 *   }
 * }
 *
 * 상위 5단계만 추출하여 OrderBookCache 초기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BithumbOrderBookSnapshotHandler {

    private static final int DEPTH = 5;

    private final OrderBookCache orderBookCache;
    private final DirtyTracker dirtyTracker;

    public void handle(JsonNode root) {
        try {
            JsonNode content = root.path("content");
            String   symbol  = content.path("symbol").asText();  // "BTC_KRW"
            String   ticker  = symbol.replace("_KRW", "");       // "BTC"

            List<OrderBookDto.OrderBookEntry> asks = parseArray(content.path("asks"), true);
            List<OrderBookDto.OrderBookEntry> bids = parseArray(content.path("bids"), false);

            OrderBookDto dto = new OrderBookDto(ticker, asks, bids);

            // 전체 스냅샷으로 캐시 초기화
            orderBookCache.put(ticker, dto);
            dirtyTracker.markDirty(ticker);
        } catch (Exception e) {
            log.warn("[Snapshot] 처리 오류: {}", e.getMessage());
        }
    }

    // 빗썸이 이미 정렬해서 주므로 정렬 불필요
    private List<OrderBookDto.OrderBookEntry> parseArray(JsonNode node, boolean isAsk) {
        List<OrderBookDto.OrderBookEntry> entries = new ArrayList<>();

        if (!node.isArray()) return entries;

        for (JsonNode item : node) {
            if (!item.isArray() || item.size() < 2) continue;
            long   price = (long) Double.parseDouble(item.get(0).asText());
            double qty   = Double.parseDouble(item.get(1).asText());
            if (price > 0 && qty > 0) {
                entries.add(new OrderBookDto.OrderBookEntry(price, qty));
            }
        }

        return entries.size() > DEPTH ? entries.subList(0, DEPTH) : entries;
    }
}
