package site.tradelink.tradelink.stock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.stock.response.ExchangeRateChartPointDto;
import site.tradelink.tradelink.stock.response.ExchangeRateSummaryDto;
import site.tradelink.tradelink.stock.service.ExchangeRateService;
import site.tradelink.tradelink.stock.sse.SseEmitterManager;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final SseEmitterManager sseEmitterManager;

    /**
     * 초기 페이지 로드: 최신 환율 조회 (DB 조회 feat. polling 방식)
     */
    @GetMapping
    public ApiResponse<List<ExchangeRateSummaryDto>> getLatestExchangeRates() {
        return ApiResponse.ok(exchangeRateService.getLatestExchangeRates());
    }

    /**
     * 테이블용 환율 조회
     */
    @GetMapping("/{currencyCode}/table")
    public ApiResponse<List<ExchangeRateSummaryDto>> getTableExchangeRates(@PathVariable String currencyCode, @RequestParam String period) {
        return ApiResponse.ok(exchangeRateService.getTableExchangeRates(currencyCode, period));
    }

    /**
     * 차트용 환율 조회
     */
    @GetMapping("/{currencyCode}/chart")
    public ApiResponse<List<ExchangeRateChartPointDto>> getChartExchangeRates(@PathVariable String currencyCode, @RequestParam String period) {
        return ApiResponse.ok(exchangeRateService.getChartExchangeRates(currencyCode, period));
    }

    /**
     * SSE 구독
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToExchangeRates(@RequestParam(required = false) String clientId) {
        if (clientId == null || clientId.isBlank()) {
            clientId = UUID.randomUUID().toString();
        }

        return sseEmitterManager.connect(clientId);
    }
}
