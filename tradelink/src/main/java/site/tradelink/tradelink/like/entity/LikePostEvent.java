package site.tradelink.tradelink.like.entity;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.like.common.enums.ActionType;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikePostEvent extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_post_event_seq")
    private Long seq;

    @Column(nullable = false)
    private Long memberSeq;

    @Column(nullable = false)
    private Long postSeq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;
}
