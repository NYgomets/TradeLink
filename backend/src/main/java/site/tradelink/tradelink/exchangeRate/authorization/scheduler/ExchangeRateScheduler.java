package site.tradelink.tradelink.exchangeRate.authorization.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.exchangeRate.entity.CurrentExchangeRate;
import site.tradelink.tradelink.exchangeRate.service.ExchangeRateDataService;
import site.tradelink.tradelink.exchangeRate.service.ExchangeRateUpdateService;

import java.util.concurrent.Executors;

/**
 * 환율 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateUpdateService updateService;
    private final ExchangeRateDataService dataService;

    /**
     * 환율 업데이트
     */
    @Scheduled(cron = "${scheduler.exchange-rate.update-cron:0 * * * * MON-FRI}")
    public void updateExchangeRate() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Currency currency : Currency.values()) {
                executor.submit(() -> {
                    try {
                        // 1. API 호출 + DB 저장
                        CurrentExchangeRate updated = updateService.updateCurrency(currency);
                    } catch (Exception e) {
                        log.error("Failed to update {}: {}", currency.name(), e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * 종가 저장
     */
    @Scheduled(cron = "${scheduler.exchange-rate.daily-close-cron:0 0 19 * * MON-FRI}")
    public void saveDailyClosingRates() {
        try {
            dataService.saveDailyClosingRates();
        } catch (Exception e) {
            log.error("Error during daily closing rate save", e);
        }
    }
}
