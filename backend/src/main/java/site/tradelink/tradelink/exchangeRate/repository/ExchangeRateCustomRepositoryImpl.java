package site.tradelink.tradelink.exchangeRate.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.ExchangeRate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExchangeRateCustomRepositoryImpl implements ExchangeRateCustomRepository {

    private final EntityManager em;

    // 특정 통화의 가장 최근 기록된 데이터 1건을 가져와서 날짜를 파악
    @Override
    public Optional<ExchangeRate> findFirstByCurrencyCodeOrderByBaseDateTimeDesc(String currencyCode) {
        List<ExchangeRate> result = em.createQuery(
                        "SELECT e FROM ExchangeRate e WHERE e.currencyCode = :code ORDER BY e.baseDateTime DESC",
                        ExchangeRate.class
                )
                .setParameter("code", currencyCode)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    // 차트용: 시간순 정렬
    @Override
    public List<ExchangeRate> findByCurrencyCodeAndBaseDateTimeBetweenOrderByBaseDateTimeAsc(String code, LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                        "SELECT e FROM ExchangeRate e WHERE e.currencyCode = :code " +
                                "AND e.baseDateTime BETWEEN :start AND :end ORDER BY e.baseDateTime ASC",
                        ExchangeRate.class
                )
                .setParameter("code", code)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }
}
