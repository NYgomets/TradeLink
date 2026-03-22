package site.tradelink.tradelink.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.tradelink.tradelink.post.common.enums.PostStatus;
import site.tradelink.tradelink.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * Query에 PostStatus.ACTIVE를 활성화 해야함
     */

    // 게시글 상세 조회
    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.member
            WHERE p.seq = :postSeq
            AND p.status = 'ACTIVE'
            """)
    Optional<Post> findActivePostWithDetailsBySeq(@Param("postSeq") Long postSeq);

    // 수정용 or post softDelete 전환
    @Query("""
            SELECT p FROM Post p
            WHERE p.seq = :postSeq
            AND p.member.seq = :memberSeq
            AND p.status = 'ACTIVE'
            """)
    Optional<Post> findActivePostBySeqAndMemberSeq(@Param("postSeq") Long poseSeq, @Param("memberSeq") Long memberSeq);

    /**
     * soft-delete된 게시글들 중 특정 시간 이전에 삭제된 것들을 조회
     * @param status DELETED 상태
     * @param cutoffDate 기준 시간
     * @return List<Post>
     */
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = :status
            AND p.deletedTime < :cutoffDate
            """)
    List<Post> findByStatusAndDeletedTimeBefore(@Param("status") PostStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("""
    SELECT p FROM Post p
    JOIN FETCH p.member
    WHERE p.status = 'ACTIVE'
    ORDER BY p.createTime DESC
    """)
    Page<Post> findActivePostsWithMember(Pageable pageable);
}
