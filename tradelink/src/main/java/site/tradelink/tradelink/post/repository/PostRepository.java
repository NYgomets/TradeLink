package site.tradelink.tradelink.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllWithUploadFiles();
}
