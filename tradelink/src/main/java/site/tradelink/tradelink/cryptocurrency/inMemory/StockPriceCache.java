package site.tradelink.tradelink.cryptocurrency.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.StockPriceSummaryDto;
import site.tradelink.tradelink.cryptocurrency.dto.TradeLogDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주식 시세 인메모리 캐시
 * 현재가 + 체결내역 담당
 */
@Slf4j
@Component
public class StockPriceCache {

    private static final int MAX_TRADE_LOG = 50;

    // 현재가 캐시
    private final Map<String, StockPriceSummaryDto> priceCache = new ConcurrentHashMap<>();

    // 체결내역 캐시 (종목별 최근 50건)
    private final Map<String, LinkedList<TradeLogDto>> tradeLogCache = new ConcurrentHashMap<>();

    // 현재가
    public Optional<StockPriceSummaryDto> findPrice(String ticker) {
        return Optional.ofNullable(priceCache.get(ticker));
    }

    public Collection<StockPriceSummaryDto> findAllPrices() {
        return Collections.unmodifiableCollection(priceCache.values());
    }

    public void putPrice(String ticker, StockPriceSummaryDto dto) {
        priceCache.put(ticker, dto);
    }

    /**
     * 체결내역
     * 체결 발생 시 앞에 추가 (최신순 유지) MAX_TRADE_LOG 초과 시 가장 오래된 항목 제거
     */
    public void addTradeLog(String ticker, TradeLogDto log) {
        LinkedList<TradeLogDto> list = tradeLogCache.computeIfAbsent(ticker, k -> new LinkedList<>());

        synchronized (list) {
            list.addFirst(log);
            if (list.size() > MAX_TRADE_LOG) {
                list.pollLast();
            }
        }
    }

    // 최근 체결내역 조회
    public List<TradeLogDto> findTradeLogs(String ticker) {
        LinkedList<TradeLogDto> list = tradeLogCache.get(ticker);
        if (list == null) return List.of();

        synchronized (list) {
            return List.copyOf(list);
        }
    }

}
