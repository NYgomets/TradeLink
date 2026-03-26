package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {

    // ticker별 lastSeq 이후 이벤트
    @Query("""
            SELECT e FROM OrderEvent e
            WHERE e.ticker = :ticker
              AND e.seq    > :lastSeq
            ORDER BY e.seq ASC
            LIMIT :limit
            """)
    List<OrderEvent> findByTickerAfterSeq(
            @Param("ticker")  String ticker,
            @Param("lastSeq") long   lastSeq,
            @Param("limit")   int    limit
    );
}
