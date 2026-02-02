package site.tradelink.tradelink.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.stock.entity.ExchangeRate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    // 특정 통화의 가장 최근 기록된 데이터 1건을 가져와서 날짜를 파악
    Optional<ExchangeRate> findFirstByCurrencyCodeOrderByBaseDateTimeDesc(String currencyCode);

    // 차트용: 시간순 정렬
    List<ExchangeRate> findByCurrencyCodeAndBaseDateTimeBetweenOrderByBaseDateTimeAsc(
            String code, LocalDateTime start, LocalDateTime end);

}
