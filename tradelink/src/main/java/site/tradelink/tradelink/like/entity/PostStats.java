package site.tradelink.tradelink.like.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        indexes = {
                @Index(name = "idx_post_stats_post_seq", columnList = "post_seq")
        }
)
public class PostStats extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_stats_seq")
    private Long seq;

    @Column(nullable = false)
    private Long postSeq;

    @Column(nullable = false)
    private Long likeCount;

    public void setLikeCount(Long likeCount) {
        this.likeCount = Math.max(0, likeCount);
    }
}
