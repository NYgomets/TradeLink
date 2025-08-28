package site.tradelink.tradelink.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllWithUploadFiles();

    Optional<Post> findBySeqAndMemberSeq(Long postSeq, Long memberSeq);
}
