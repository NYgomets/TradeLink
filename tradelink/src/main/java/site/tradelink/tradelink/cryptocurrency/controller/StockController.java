package site.tradelink.tradelink.cryptocurrency.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;
import site.tradelink.tradelink.cryptocurrency.dto.StockPriceSummaryDto;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.inMemory.StockPriceCache;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockPriceCache priceCache;
    private final OrderBookCache orderBookCache;
    private final SseEmitterManager sseManager;

    // 종목 목록 (클라이언트 5초 polling)
    @GetMapping
    public ApiResponse<List<StockPriceSummaryDto>> getAll() {
        return ApiResponse.ok(
                priceCache.findAllPrices().stream()
                        .sorted(Comparator.comparingLong(StockPriceSummaryDto::price).reversed())
                        .toList()
        );
    }

    // 종목 상세 초기 데이터

    @GetMapping("/{ticker}/price")
    public ApiResponse<StockPriceSummaryDto> getPrice(@PathVariable String ticker) {
        return ApiResponse.ok(
                priceCache.findPrice(ticker)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 종목: " + ticker))
        );
    }

    @GetMapping("/{ticker}/orderbook")
    public ApiResponse<OrderBookDto> getOrderBook(@PathVariable String ticker) {
        return ApiResponse.ok(
                orderBookCache.findTop5(ticker)
                        .orElseThrow(() -> new IllegalArgumentException("호가 데이터 없음: " + ticker))
        );
    }

    // SSE 구독

    /**
     * 종목 상세 SSE 구독 연결 즉시 현재가 + 호가(5단계) 초기 push
     * 이후 stock-price / order-book 이벤트 수신 (1초 throttle)
     */
    @GetMapping(value = "/sse/{ticker}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToTicker(
            @PathVariable String ticker,
            @RequestParam(required = false) String clientId) {

        if (clientId == null || clientId.isBlank()) clientId = UUID.randomUUID().toString();
        return sseManager.connectTicker(clientId, ticker);
    }
}
