package site.tradelink.tradelink.like.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.LikePostEvent;

import java.util.List;

@Repository
public interface LikePostEventRepository extends JpaRepository<LikePostEvent, Long> {

    @Query("SELECT e FROM LikePostEvent e WHERE e.seq > :lastPostEventSeq ORDER BY e.seq ASC")
    List<LikePostEvent> findEventsAfterCursor(@Param("lastPostEventSeq") Long lastPostEventSeq, Pageable pageable);
}
