package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.TradeHistory;

import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    List<TradeHistory> findByMemberSeqOrderByCreateTimeDesc(Long memberSeq);
}
