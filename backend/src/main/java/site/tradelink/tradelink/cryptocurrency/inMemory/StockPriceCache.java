package site.tradelink.tradelink.cryptocurrency.inMemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.dto.StockPriceSummaryDto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 주식 시세 인메모리 캐시
 * 현재가 + 체결내역 담당
 */
@Slf4j
@Component
public class StockPriceCache {

    // 현재가 캐시
    private final Map<String, StockPriceSummaryDto> priceCache = new ConcurrentHashMap<>();

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
}
