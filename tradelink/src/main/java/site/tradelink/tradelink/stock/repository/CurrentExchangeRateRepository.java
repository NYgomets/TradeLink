package site.tradelink.tradelink.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.stock.entity.CurrentExchangeRate;

import java.util.Optional;

@Repository
public interface CurrentExchangeRateRepository extends JpaRepository<CurrentExchangeRate, Long> {

    Optional<CurrentExchangeRate> findByCurrencyCode(String currencyCode);
}
