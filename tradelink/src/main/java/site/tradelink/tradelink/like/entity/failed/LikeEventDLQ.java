package site.tradelink.tradelink.like.entity.failed;

import jakarta.persistence.*;
import lombok.*;
import site.tradelink.tradelink.supports.entity.BaseEntity;
import site.tradelink.tradelink.like.common.enums.ActionType;

/**
 * DLQ 테이블 특성상 인덱스 미적용
 * - 정상 시스템에서 데이터 극소량 → 풀 스캔 비용 무의미
 * - INSERT/DELETE 시 인덱스 유지 비용이 조회 이득보다 큼
 * - RETRY_BATCH_SIZE = 10으로 처리량 자체가 적음
 *
 * @Version 기반 낙관적 락 검토 후 미적용 결정
 * - ProcessLikeEvents(1초)와 DLQRetryScheduler(60초) 동시 접근 확률 매우 낮음
 * - 충돌 발생 시 최악의 결과가 retryCount 1 오차로 시스템 장애로 이어지지 않음
 * - 도입 시 트랜잭션 분리 및 별도 빈 생성 필요로 복잡도 대비 실익 없음
 */
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
