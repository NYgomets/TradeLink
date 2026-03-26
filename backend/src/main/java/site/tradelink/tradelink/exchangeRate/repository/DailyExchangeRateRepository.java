package site.tradelink.tradelink.exchangeRate.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.exchangeRate.entity.DailyExchangeRate;

@Repository
public interface DailyExchangeRateRepository extends JpaRepository<DailyExchangeRate, Long>, DailyExchangeRateCustomRepository {

}
