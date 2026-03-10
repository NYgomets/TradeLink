package site.tradelink.tradelink.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.stock.entity.DailyExchangeRate;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyExchangeRateRepository extends JpaRepository<DailyExchangeRate, Long>, DailyExchangeRateCustomRepository {

}
