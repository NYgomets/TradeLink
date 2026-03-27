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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyCloseRecovery implements ApplicationRunner {

    private final ExchangeRateDataService dataService;
    private final DailyExchangeRateRepository dailyRepository;
    private final ExchangeRateRepository historyRepository;

    @Override
    public void run(ApplicationArguments args) {

        // 최근 7일 안에서 가장 최근 영업일 찾기 (공휴일/휴일 대응)
        LocalDate targetDate = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .filter(date -> {
                    LocalDateTime s = date.atStartOfDay();
                    LocalDateTime e = date.atTime(23, 59, 59);
                    return Arrays.stream(Currency.values()).anyMatch(c ->
                            historyRepository.existsByCurrencyCodeAndBaseDateTimeBetween(
                                    c.getCurrencyCode(), s, e));
                })
                .findFirst()
                .orElse(null);

        if (targetDate == null) {
            log.info("최근 7일간 환율 데이터 없음 → 복구 스킵");
            return;
        }

        Set<String> savedCodes = Arrays.stream(Currency.values())
                .filter(c -> dailyRepository.existsByCurrencyCodeAndBaseDate(
                        c.getCurrencyCode(), targetDate))
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());

        // 누락된 통화만 필터링
        List<Currency> missingCurrencies = Arrays.stream(Currency.values())
                .filter(c -> !savedCodes.contains(c.getCurrencyCode()))
                .toList();

        if (missingCurrencies.isEmpty()) {
            log.info("최근 영업일({}) 종가 모두 존재 → 복구 스킵", targetDate);
            return;
        }

        log.info("최근 영업일({}) 종가 누락 감지 {} → 자동 복구 실행", targetDate, missingCurrencies);
        dataService.recoverDailyClosingRates(targetDate, missingCurrencies);
    }
}