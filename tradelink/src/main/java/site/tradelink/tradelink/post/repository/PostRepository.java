package site.tradelink.tradelink.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.common.enums.PostStatus;
import site.tradelink.tradelink.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Query에 PostStatus.ACTIVE를 활성화 해야함
    Optional<Post> findActivePostWithDetailsBySeq(Long postSeq);
    Optional<Post> findActivePostWithFilesBySeqAndMemberSeq(Long poseSeq, Long memberSeq);
    Optional<Post> findActivePostBySeqAndMemberSeq(Long postSeq, Long memberSeq);

    /**
     * soft-delete된 게시글들 중 특정 시간 이전에 삭제된 것들을 조회
     * @param status DELETED 상태
     * @param cutoffDate 기준 시간
     * @return List<Post>
     */
    List<Post> findByStatusAndDeletedTimeBefore(PostStatus status, LocalDateTime cutoffDate);
}
