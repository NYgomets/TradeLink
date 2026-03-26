package site.tradelink.tradelink.cryptocurrency.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주식 호가 인메모리 캐시
 * 30호가 전체 보관
 * 클라이언트(SSE): findTop5()로 상위 5단계만 잘라서 전송
 */
@Slf4j
@Component
public class OrderBookCache {

    private static final int SSE_DEPTH = 5;

    @Value("${stock.orderbook.stale-ttl-ms:5000}")
    private long staleTtlMs;

    // 호가 + 수신 시각을 하나로 묶어 원자적 업데이트
    private record CachedOrderBook(OrderBookDto bookDto, Instant receivedAt) {}

    public record DepthUpdate(String orderType, long price, double quantity) {}

    private final Map<String, CachedOrderBook> cache = new ConcurrentHashMap<>();

    // snapshot 수신 시 전체 교체
    public void put(String ticker, OrderBookDto orderBook) {
        cache.put(ticker, new CachedOrderBook(orderBook, Instant.now()));
    }

    // 내부 전체 호가 조회
    public Optional<OrderBookDto> find(String ticker) {
        return Optional.ofNullable(cache.get(ticker))
                .map(CachedOrderBook::bookDto);
    }

    // SSE broadcast 용: 상위 5단계만 잘라서 반환
    public Optional<OrderBookDto> findTop5(String ticker) {
        return Optional.ofNullable(cache.get(ticker))
                .map(cache -> {
                    List<OrderBookDto.OrderBookEntry> asks = cache.bookDto.asks();
                    List<OrderBookDto.OrderBookEntry> bids = cache.bookDto.bids();

                    return new OrderBookDto(
                            ticker,
                            asks.size() > SSE_DEPTH ? asks.subList(0, SSE_DEPTH) : asks,
                            bids.size() > SSE_DEPTH ? bids.subList(0, SSE_DEPTH) : bids
                    );
                });
    }

    // orderbookdepth 변경분 / 실제 변경이 발생한 경우에만 true 반환 -> dirty 표시 여부 결정
    public boolean merge(String ticker, List<DepthUpdate> updates) {
        boolean[] changed = {false};

        cache.computeIfPresent(ticker, (k, cached) -> {
            List<OrderBookDto.OrderBookEntry> asks = new ArrayList<>(cached.bookDto().asks());
            List<OrderBookDto.OrderBookEntry> bids = new ArrayList<>(cached.bookDto().bids());

            for (DepthUpdate update : updates) {
                List<OrderBookDto.OrderBookEntry> target = "ask".equals(update.orderType()) ? asks : bids;
                boolean updated = applyUpdate(target, update.price(), update.quantity());
                if (updated) changed[0] = true;
            }

            if (!changed[0]) return cached; // 변경 없으면 기존 그대로 반환

            asks.sort((a, b) -> Long.compare(a.price(), b.price()));
            bids.sort((a, b) -> Long.compare(b.price(), a.price()));

            // 30호가 전체 보관 (자르지 않음)
            OrderBookDto merged = new OrderBookDto(ticker,
                    new ArrayList<>(asks),
                    new ArrayList<>(bids));

            return new CachedOrderBook(merged, Instant.now());
        });

        return changed[0];
    }

    // 체결가 조회 (stale 체크 포함 / stale 상태면 empty 반환 -> 주문 거부
    public OptionalLong getBestPrice(String ticker, OrderSide side) {
        if (isStale(ticker)) {
            log.warn("[OrderBookCache] {} 호가 stale ({}ms 초과)", ticker, staleTtlMs);
            return OptionalLong.empty();
        }

        CachedOrderBook cached = cache.get(ticker);
        if (cached == null) {
            return OptionalLong.empty();
        }

        List<OrderBookDto.OrderBookEntry> levels = OrderSide.BUY == side ? cached.bookDto.asks() : cached.bookDto.bids();

        if (levels == null || levels.isEmpty()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(levels.get(0).price());
    }

    public boolean isStale(String ticker) {
        CachedOrderBook cached = cache.get(ticker);

        if (cached == null) return true;

        return Instant.now().toEpochMilli() - cached.receivedAt().toEpochMilli() > staleTtlMs;
    }

    public Optional<Instant> getReceivedAt(String ticker) {
        return Optional.ofNullable(cache.get(ticker))
                .map(CachedOrderBook::receivedAt);
    }

    /**
     * 내부 유틸
     * 단일 호가 레벨 업데이트
     * quantity == 0 -> 제거
     * quantitiy > 0 -> 잔량 교체
     *
     * 반환: 실제 변경 발생 여부
     */
    private boolean applyUpdate(List<OrderBookDto.OrderBookEntry> entries, long price, double quantity) {
        boolean existed = entries.removeIf(e -> e.price() == price);

        if (quantity > 0) {
            entries.add(new OrderBookDto.OrderBookEntry(price, quantity));
            return true;
        }

        return existed; // quantity==0 이면 제거됐을 때만 변경으로 간주
    }

}
