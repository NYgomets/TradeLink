package site.tradelink.tradelink.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.stock.entity.ExchangeRate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long>, ExchangeRateCustomRepository {

}
