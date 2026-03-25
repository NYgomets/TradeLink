package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.TradeHistory;

import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    // 첫 페이지용 (cursor 없을 때)
    Slice<TradeHistory> findByMemberSeqOrderBySeqDesc(Long memberSeq, Pageable pageable);

    // Cursor 방식: 마지막으로 본 seq보다 작은 것 조회
    Slice<TradeHistory> findByMemberSeqAndSeqLessThanOrderBySeqDesc(
            Long memberSeq, Long cursorSeq, Pageable pageable);
}
