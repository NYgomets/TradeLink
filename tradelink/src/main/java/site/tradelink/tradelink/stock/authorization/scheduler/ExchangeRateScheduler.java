package site.tradelink.tradelink.stock.authorization.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.stock.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;
import site.tradelink.tradelink.stock.response.ExchangeRateSummaryDto;
import site.tradelink.tradelink.stock.service.ExchangeRateDataService;
import site.tradelink.tradelink.stock.service.ExchangeRateUpdateService;
import site.tradelink.tradelink.stock.sse.SseEmitterManager;

/**
 * 환율 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateUpdateService updateService;
    private final ExchangeRateDataService dataService;
    private final SseEmitterManager sseEmitterManager;

    /**
     * 환율 업데이트
     */
    @Scheduled(cron = "${scheduler.exchange-rate.update-cron:0 */10 * * * MON-FRI}")
    public void updateExchangeRate() {
        for (Currency currency : Currency.values()) {
            try {
                // 1. API 호출 + DB 저장
                CurrentExchangeRate updated = updateService.updateCurrency(currency);

                // 2. SSE 브로드캐스트
                broadcastExchangeRate(updated);
            } catch (Exception e) {
                log.error("Failed to update {}: {}", currency.name(), e.getMessage());
            }
        }
    }

    /**
     * 종가 저장
     */
    @Scheduled(cron = "${scheduler.exchange-rate.daily-close-cron:0 0 10 * * MON-FRI}")
    public void saveDailyClosingRates() {
        try {
            dataService.saveDailyClosingRates();
        } catch (Exception e) {
            log.error("Error during daily closing rate save", e);
        }
    }

    /**
     * SSE 브로드캐스트
     */
    private void broadcastExchangeRate(CurrentExchangeRate current) {
        try {
            ExchangeRateSummaryDto dto = ExchangeRateSummaryDto.fromCurrent(current);
            sseEmitterManager.broadcast("exchange-rate", dto);
        } catch (Exception e) {
            log.warn("Failed to broadcast SSE for {}: {}", current.getCurrencyCode(), e.getMessage());
        }
    }
}
