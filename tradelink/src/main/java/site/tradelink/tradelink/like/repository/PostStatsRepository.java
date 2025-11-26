package site.tradelink.tradelink.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.PostStats;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostStatsRepository extends JpaRepository<PostStats, Long> {
    Optional<PostStats> findByPostSeq(Long postSeq);

    @Query("SELECT ps FROM PostStats ps WHERE ps.postSeq IN :postSeqs")
    List<PostStats> findAllByPostSeqs(@Param("postSeqs") List<Long> postSeqs);
}
