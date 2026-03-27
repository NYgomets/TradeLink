package site.tradelink.tradelink.exchangeRate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.ExchangeRate;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long>, ExchangeRateCustomRepository {
    boolean existsByCurrencyCodeAndBaseDateTimeBetween(
            String currencyCode, LocalDateTime start, LocalDateTime end);

    Optional<ExchangeRate> findTopByCurrencyCodeAndBaseDateTimeBetweenOrderByBaseDateTimeDesc(
            String currencyCode, LocalDateTime start, LocalDateTime end);
}
