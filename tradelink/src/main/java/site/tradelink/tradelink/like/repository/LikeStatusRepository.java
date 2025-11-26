package site.tradelink.tradelink.like.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.LikeStatus;

import java.util.Optional;

@Repository
public interface LikeStatusRepository extends JpaRepository<LikeStatus, Long> {
    Optional<LikeStatus> findByMemberSeqAndPostSeq(Long memberSeq, Long postSeq);

    // 1. isLiked가 true인 것만 조회
    // 2. 페이징 처리 (Pageable)
    // 3. 엔티티 전체가 아닌 postSeq만 조회
    @Query("SELECT ls.postSeq FROM LikeStatus ls WHERE ls.memberSeq = :memberSeq AND ls.isLiked = true ORDER BY ls.modifiedTime DESC")
    Page<Long> findLikedPostSeqs(@Param("memberSeq") Long memberSeq, Pageable pageable);
}
