package site.tradelink.tradelink.exchangeRate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyExchangeRateRepository extends JpaRepository<DailyExchangeRate, Long>, DailyExchangeRateCustomRepository {
    boolean existsByCurrencyCodeAndBaseDate(String currencyCode, LocalDate baseDate);

    Optional<DailyExchangeRate> findTopByCurrencyCodeAndBaseDateBeforeOrderByBaseDateDesc(String currencyCode, LocalDate today);
}
