package site.tradelink.tradelink.cryptocurrency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.cryptocurrency.entity.Holding;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByMemberSeqAndTicker(Long memberSeq, String ticker);
    List<Holding> findByMemberSeq(Long memberSeq);
}
