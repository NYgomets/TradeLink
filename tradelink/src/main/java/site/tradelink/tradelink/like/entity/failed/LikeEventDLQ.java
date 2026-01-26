package site.tradelink.tradelink.like.entity.failed;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.common.entity.BaseEntity;
import site.tradelink.tradelink.like.common.enums.ActionType;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeEventDLQ extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dlq_seq")
    private Long seq;

    @Column(nullable = false)
    private Long originalEventSeq;

    @Column(nullable = false)
    private Long memberSeq;

    @Column(nullable = false)
    private Long postSeq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false)
    private Integer retryCount;

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
