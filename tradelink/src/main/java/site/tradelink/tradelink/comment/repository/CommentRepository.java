package site.tradelink.tradelink.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.tradelink.tradelink.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * seq로 댓글을 조회할 때, 연관된 부모 댓글까지 함께 가져와 1+1 문제를 해결
     */
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.parent WHERE c.seq = :commentSeq")
    Optional<Comment> findByIdWithParent(@Param("commentSeq") Long commentSeq);

    /**
     * 특정 게시글의 모든 댓글 조회
     * n+1 문제 해결하기 위해 member와 parent를 fetch join 전략 사용
     * parent는 원댓글의 경우 NULL일 수 있으므로, LEFT JOIN FETCH를 사용
     */
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.member m " +
            "LEFT JOIN FETCH c.parent p " +
            "WHERE c.post.seq = :postSeq " +
            "ORDER BY c.parent.seq ASC NULLS FIRST, c.createTime ASC")
    List<Comment> findAllByPostSeq(@Param("postSeq") Long postSeq);

    @Query("SELECT c FROM Comment c WHERE c.seq = :commentSeq AND c.member.seq = :memberSeq AND c.status = 'ACTIVE'")
    Optional<Comment> findActiveCommentBySeqAndMemberSeq(@Param("commentSeq") Long commentSeq, @Param("memberSeq") Long memberSeq);

    // 추후 QueryDsl로 리팩토링 예정
    /**
     * 영구 삭제할 댓글 조회. 아래 조건을 모두 만족해야 한다.
     * 1. 상태가 'DELETED' (소프트 삭제 상태)
     * 2. 삭제된 지 N일이 지남 (cutoffDate 이전)
     * 3. 자신을 부모로 하는 'ACTIVE' 상태의 자식 댓글이 존재하지 않음 (대화 맥락 보존을 위한 핵심 로직)
     */
    @Query("SELECT c FROM Comment c WHERE c.status = 'DELETED' AND c.deletedTime < :cutoffDate " +
            "AND NOT EXISTS (SELECT child FROM Comment child WHERE child.parent = c AND child.status = 'ACTIVE')")
    List<Comment> findPurgableComments(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.seq = :postSeq AND c.status = 'ACTIVE'")
    int countByPostSeq(@Param("postSeq") Long postSeq);
}
