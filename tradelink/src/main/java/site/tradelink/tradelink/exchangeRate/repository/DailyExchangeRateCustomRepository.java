package site.tradelink.tradelink.exchangeRate.repository;

import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;

import java.time.LocalDate;
import java.util.List;

public interface DailyExchangeRateCustomRepository {

    // 표(Table)용: 날짜별 최종 마감 데이터만 리스트로 조회 (최신 -> 과거)
    List<DailyExchangeRate> findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateDesc(
            String code, LocalDate start, LocalDate end);

    // Chart용: 날짜별 최종 마감 데이터만 리스트로 조회 (과거 -> 최신)
    List<DailyExchangeRate> findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateAsc(
            String code, LocalDate start, LocalDate end);
}
