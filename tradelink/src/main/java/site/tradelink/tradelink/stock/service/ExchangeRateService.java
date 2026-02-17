package site.tradelink.tradelink.stock.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.stock.entity.ExchangeRate;
import site.tradelink.tradelink.stock.repository.CurrentExchangeRateRepository;
import site.tradelink.tradelink.stock.repository.DailyExchangeRateRepository;
import site.tradelink.tradelink.stock.repository.ExchangeRateRepository;
import site.tradelink.tradelink.stock.response.ExchangeRateChartPointDto;
import site.tradelink.tradelink.stock.response.ExchangeRateSummaryDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final CurrentExchangeRateRepository currentRepository;
    private final ExchangeRateRepository historyRepository;
    private final DailyExchangeRateRepository dailyRepository;

    /**
     * 최신 환율 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeRateSummaryDto> getLatestExchangeRates() {
        return currentRepository.findAll().stream()
                .map(ExchangeRateSummaryDto::fromCurrent)
                .toList();
    }

    /**
     * 테이블용
     * 클라이언트가 보낸 일 수(period)를 기반으로 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeRateSummaryDto> getTableExchangeRates(String currencyCode, String period) {

        int days = parsePeriod(period);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);

        return dailyRepository.findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateDesc(currencyCode, start, end)
                .stream()
                .map(ExchangeRateSummaryDto::fromDaily)
                .toList();
    }

    /**
     * 차트용
     * 클라이언트가 보낸 일 수(period)를 기반으로 조회
     * 임계값 1일: 1일은 당일 00시 부터 상세 로그, 그 외는 일별 종가 테이블 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeRateChartPointDto> getChartExchangeRates(String currencyCode, String period) {

        int days = parsePeriod(period);

        // 1. 당일(1일) 요청인 경우: 상세 로그 테이블(ExchangeRate) 조회
        if (days <= 1) {
            // 가장 최근 데이터의 시점을 확인 (주말/공휴일 대응)
            LocalDateTime latestPoint = historyRepository.findFirstByCurrencyCodeOrderByBaseDateTimeDesc(currencyCode)
                    .map(ExchangeRate::getBaseDateTime)
                    .orElse(LocalDateTime.now());

            // 해당 영업일의 00:00:00 ~ 현재(혹은 데이터의 마지막 지점) 조회
            LocalDateTime start = latestPoint.toLocalDate().atStartOfDay();
            LocalDateTime end = latestPoint; // 마지막 데이터 시점까지

            return historyRepository.findByCurrencyCodeAndBaseDateTimeBetweenOrderByBaseDateTimeAsc(currencyCode, start, end)
                    .stream()
                    .map(ExchangeRateChartPointDto::fromHistory)
                    .toList();
        }

        // 2. 1일 초과 요청인 경우: 일별 종가 테이블(DailyExchangeRate) 조회
        else {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(days);

            return dailyRepository.findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateAsc(currencyCode, start, end)
                    .stream()
                    .map(ExchangeRateChartPointDto::fromDaily)
                    .toList();
        }
    }

    private int parsePeriod(String period) {
        try {
            return Integer.parseInt(period);
        } catch (NumberFormatException e) {
            return 1; // 기본값 당일 조회
        }
    }
}
