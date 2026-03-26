package site.tradelink.tradelink.exchangeRate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import site.tradelink.tradelink.exchangeRate.response.ExchangeRateChartPointDto;
import site.tradelink.tradelink.exchangeRate.response.ExchangeRateSummaryDto;
import site.tradelink.tradelink.exchangeRate.service.ExchangeRateService;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

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
}
