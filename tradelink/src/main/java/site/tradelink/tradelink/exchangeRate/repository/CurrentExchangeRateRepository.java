package site.tradelink.tradelink.exchangeRate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.CurrentExchangeRate;

import java.util.Optional;

@Repository
public interface CurrentExchangeRateRepository extends JpaRepository<CurrentExchangeRate, Long>, CurrentExchangeRateCustomRepository {

    Optional<CurrentExchangeRate> findByCurrencyCode(String currencyCode);
}
