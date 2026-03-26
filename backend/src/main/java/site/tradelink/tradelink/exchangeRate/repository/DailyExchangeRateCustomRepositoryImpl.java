package site.tradelink.tradelink.exchangeRate.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DailyExchangeRateCustomRepositoryImpl implements DailyExchangeRateCustomRepository{

    private final EntityManager em;

    // 표(Table)용: 날짜별 최종 마감 데이터만 리스트로 조회 (최신 -> 과거)
    @Override
    public List<DailyExchangeRate> findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateDesc(String code, LocalDate start, LocalDate end) {
        return em.createQuery(
                        "SELECT d FROM DailyExchangeRate d WHERE d.currencyCode = :code " +
                                "AND d.baseDate BETWEEN :start AND :end ORDER BY d.baseDate DESC",
                        DailyExchangeRate.class
                )
                .setParameter("code", code)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    // Chart용: 날짜별 최종 마감 데이터만 리스트로 조회 (과거 -> 최신)
    @Override
    public List<DailyExchangeRate> findByCurrencyCodeAndBaseDateBetweenOrderByBaseDateAsc(String code, LocalDate start, LocalDate end) {
        return em.createQuery(
                        "SELECT d FROM DailyExchangeRate d WHERE d.currencyCode = :code " +
                                "AND d.baseDate BETWEEN :start AND :end ORDER BY d.baseDate ASC",
                        DailyExchangeRate.class
                )
                .setParameter("code", code)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}
