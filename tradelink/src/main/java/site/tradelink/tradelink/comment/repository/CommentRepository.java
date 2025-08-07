package site.tradelink.tradelink.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
