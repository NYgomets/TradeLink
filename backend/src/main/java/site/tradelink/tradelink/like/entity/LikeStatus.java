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
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_post", columnNames = {"member_seq", "post_seq"})
        }
)
public class LikeStatus extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_status_seq")
    private Long seq;

    @Column(nullable = false)
    private Long memberSeq;

    @Column(nullable = false)
    private Long postSeq;
}
