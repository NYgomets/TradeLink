package site.tradelink.tradelink.like.repository.failed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.failed.LikeEventDLQ;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeEventDLQRepository extends JpaRepository<LikeEventDLQ, Long> {

    Optional<LikeEventDLQ> findByOriginalEventSeq(Long originalEventSeq);

    @Query("SELECT d FROM LikeEventDLQ d WHERE d.retryCount < :maxRetry ORDER BY d.seq ASC")
    List<LikeEventDLQ> findRetryableEvents(@Param("maxRetry") int maxRetry);

    long countByRetryCountGreaterThanEqual(int retryCount);
}
