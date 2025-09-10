package site.tradelink.tradelink.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.entity.Post;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findActivePostWithDetailsBySeq(Long postSeq);
    Optional<Post> findActivePostWithFilesBySeqAndMemberSeq(Long poseSeq, Long memberSeq);
    Optional<Post> findActivePostBySeqAndMemberSeq(Long postSeq, Long memberSeq);
}
