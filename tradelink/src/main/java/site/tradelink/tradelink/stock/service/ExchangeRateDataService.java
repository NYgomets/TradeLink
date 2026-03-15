package site.tradelink.tradelink.stock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.stock.authorization.scheduler.enums.Currency;
import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;
import site.tradelink.tradelink.stock.entity.DailyExchangeRate;
import site.tradelink.tradelink.stock.entity.ExchangeRate;
import site.tradelink.tradelink.stock.repository.CurrentExchangeRateRepository;
import site.tradelink.tradelink.stock.repository.DailyExchangeRateRepository;
import site.tradelink.tradelink.stock.repository.ExchangeRateRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 환율 데이터 저장 서비스 (DB 작업만 담당)
 */
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
        Optional<CurrentExchangeRate> optional = currentRepository.findByCurrencyCode(currency.getItemCode());

        CurrentExchangeRate current;
        Double previous = null;

        if (optional.isEmpty()) {
            current = CurrentExchangeRate.builder()
                    .currencyCode(currency.getItemCode())
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
                .currencyCode(currency.getItemCode())
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

        List<CurrentExchangeRate> rateList = currentRepository.findAll();

        for (CurrentExchangeRate current : rateList) {
            DailyExchangeRate daily = DailyExchangeRate.fromCurrent(current, today);
            try {
                dailyRepository.save(daily);
            } catch (DataIntegrityViolationException ignored) {
                // 이미 저장된 데이터 무시
            }
        }
    }
}
