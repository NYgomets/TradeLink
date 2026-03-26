package site.tradelink.tradelink.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.PostStats;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostStatsRepository extends JpaRepository<PostStats, Long> {
    Optional<PostStats> findByPostSeq(Long postSeq);

    List<PostStats> findAllByPostSeqIn(Collection<Long> postSeqs);

    @Modifying
    @Query("UPDATE PostStats p SET p.likeCount = p.likeCount + 1 WHERE p.postSeq = :postSeq")
    int incrementLikeCount(@Param("postSeq") Long postSeq);

    @Modifying
    @Query("UPDATE PostStats p SET p.likeCount = GREATEST(p.likeCount - 1, 0) WHERE p.postSeq = :postSeq")
    int decrementLikeCount(@Param("postSeq") Long postSeq);
}
