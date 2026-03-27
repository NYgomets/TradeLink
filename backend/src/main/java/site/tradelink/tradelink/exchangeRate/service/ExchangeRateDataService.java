package site.tradelink.tradelink.exchangeRate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.exchangeRate.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.exchangeRate.entity.CurrentExchangeRate;
import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;
import site.tradelink.tradelink.exchangeRate.entity.ExchangeRate;
import site.tradelink.tradelink.exchangeRate.repository.CurrentExchangeRateRepository;
import site.tradelink.tradelink.exchangeRate.repository.DailyExchangeRateRepository;
import site.tradelink.tradelink.exchangeRate.repository.ExchangeRateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 환율 데이터 저장 서비스 (DB 작업만 담당)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateDataService {

    private final CurrentExchangeRateRepository currentRepository;
    private final ExchangeRateRepository historyRepository;
    private final DailyExchangeRateRepository dailyRepository;

    /**
     * 환율 데이터 저장
     */
    @Transactional
    public CurrentExchangeRate saveExchangeRateData(Currency currency, Double rate, LocalDateTime dateTime) {
        // 1. CurrentExchangeRate 업데이트
        CurrentExchangeRate current = updateCurrentRate(currency, rate, dateTime);

        // 2. ExchangeRate 히스토리 추가
        saveHistory(currency, rate, dateTime);

        return current;
    }

    /**
     * CurrentExchangeRate 업데이트
     */
    private CurrentExchangeRate updateCurrentRate(Currency currency, Double rate, LocalDateTime dateTime) {
        Optional<CurrentExchangeRate> optional = currentRepository.findByCurrencyCode(currency.getCurrencyCode());

        CurrentExchangeRate current;
        Double previous = null;

        if (optional.isEmpty()) {
            current = CurrentExchangeRate.builder()
                    .currencyCode(currency.getCurrencyCode())
                    .currencyName(currency.getKoreanName())
                    .rate(rate)
                    .baseDateTime(dateTime)
                    .build();

            current.updateRate(rate, previous, dateTime);
            return currentRepository.save(current);
        }

        current = optional.get();
        previous = current.getRate();

        current.updateRate(rate, previous, dateTime);

        return current;
    }

    /**
     * ExchangeRate 히스토리 저장
     */
    private void saveHistory(Currency currency, Double rate, LocalDateTime dateTime) {
        ExchangeRate history = ExchangeRate.builder()
                .currencyCode(currency.getCurrencyCode())
                .rate(rate)
                .baseDateTime(dateTime)
                .build();

        historyRepository.save(history);
    }

    /**
     * 종가 저장
     */
    @Transactional
    public void saveDailyClosingRates() {
        LocalDate today = LocalDate.now();

        // 오늘 현재가 데이터가 없으면 아예 스킵
        List<CurrentExchangeRate> rateList = currentRepository.findAll();
        if (rateList.isEmpty()) {
            log.info("오늘({}) 현재가 데이터 없음 → 종가 저장 스킵", today);
            return;
        }

        for (CurrentExchangeRate current : rateList) {
            DailyExchangeRate prevDay =
                    dailyRepository.findTopByCurrencyCodeAndBaseDateBeforeOrderByBaseDateDesc(
                            current.getCurrencyCode(), today
                    ).orElse(null);

            Double changeAmount = prevDay != null ? current.getRate() - prevDay.getRate() : 0.0;

            Double changePercent = prevDay != null ? (changeAmount / prevDay.getRate()) * 100 : 0.0;

            DailyExchangeRate daily = DailyExchangeRate.builder()
                    .currencyCode(current.getCurrencyCode())
                    .currencyName(current.getCurrencyName())
                    .rate(current.getRate())
                    .changeAmount(changeAmount)
                    .changePercent(changePercent)
                    .baseDate(today)
                    .build();

            dailyRepository.save(daily);
        }
    }

    /**
     * 종가 저장 - 복구용 (특정 날짜 히스토리 기반, 누락된 통화만 처리)
     */
    @Transactional
    public void recoverDailyClosingRates(LocalDate date, List<Currency> missingCurrencies) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        missingCurrencies.forEach(currency -> {
            historyRepository
                    .findTopByCurrencyCodeAndBaseDateTimeBetweenOrderByBaseDateTimeDesc(
                            currency.getCurrencyCode(), start, end)
                    .ifPresent(history -> {
                        // 해당 날짜 이전 가장 최근 종가 조회 (공휴일/휴일 대응)
                        DailyExchangeRate prevDay = dailyRepository
                                .findTopByCurrencyCodeAndBaseDateBeforeOrderByBaseDateDesc(
                                        currency.getCurrencyCode(), date)
                                .orElse(null);

                        Double changeAmount = prevDay != null ? history.getRate() - prevDay.getRate() : 0.0;
                        Double changePercent = prevDay != null ? (changeAmount / prevDay.getRate()) * 100 : 0.0;

                        DailyExchangeRate daily = DailyExchangeRate.builder()
                                .currencyCode(history.getCurrencyCode())
                                .currencyName(currency.getKoreanName())
                                .rate(history.getRate())
                                .changeAmount(changeAmount)
                                .changePercent(changePercent)
                                .baseDate(date)
                                .build();

                        dailyRepository.save(daily);
                    });
        });
    }
}
