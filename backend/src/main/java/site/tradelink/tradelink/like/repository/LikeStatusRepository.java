package site.tradelink.tradelink.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.tradelink.tradelink.like.entity.LikeStatus;

import java.util.Optional;

@Repository
public interface LikeStatusRepository extends JpaRepository<LikeStatus, Long> {
    Optional<LikeStatus> findByMemberSeqAndPostSeq(Long memberSeq, Long postSeq);
}
