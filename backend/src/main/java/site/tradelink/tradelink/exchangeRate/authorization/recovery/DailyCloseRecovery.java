package site.tradelink.tradelink.exchangeRate.authorization.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.exchangeRate.repository.DailyExchangeRateRepository;
import site.tradelink.tradelink.exchangeRate.repository.ExchangeRateRepository;
import site.tradelink.tradelink.exchangeRate.service.ExchangeRateDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyCloseRecovery implements ApplicationRunner {

    private final ExchangeRateDataService dataService;
    private final DailyExchangeRateRepository dailyRepository;
    private final ExchangeRateRepository historyRepository;

    @Override
    public void run(ApplicationArguments args) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime start = yesterday.atStartOfDay();
        LocalDateTime end = yesterday.atTime(23, 59, 59);

        // 주말/공휴일 체크 - 어떤 통화든 하나라도 있으면 영업일로 판단
        boolean hasYesterdayData = Arrays.stream(Currency.values())
                .anyMatch(currency ->
                        historyRepository.existsByCurrencyCodeAndBaseDateTimeBetween(
                                currency.getCurrencyCode(), start, end)
                );

        if (!hasYesterdayData) {
            log.info("어제({}) 환율 데이터 없음 (휴일 추정) → 복구 스킵", yesterday);
            return;
        }

        // 종가 누락 체크 - 하나라도 없으면 복구 실행
        boolean allExists = Arrays.stream(Currency.values())
                .allMatch(currency ->
                        dailyRepository.existsByCurrencyCodeAndBaseDate(
                                currency.getCurrencyCode(), yesterday)
                );

        if (!allExists) {
            log.info("어제({}) 종가 누락 감지 → 자동 복구 실행", yesterday);
            dataService.recoverDailyClosingRates(yesterday);
        }
    }
}