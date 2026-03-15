package site.tradelink.tradelink.like.common.scheduler.failed;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.entity.failed.LikeEventDLQ;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.repository.failed.LikeEventDLQRepository;
import site.tradelink.tradelink.like.service.LikeEventProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DLQRetryScheduler {

    private final LikeEventProcessor likeEventProcessor;

    private final LikeEventDLQRepository dlqRepository;
    private final PostStatsRepository postStatsRepository;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_BATCH_SIZE = 10;

    /**
     * DLQ에 있는 이벤트 재처리
     * - 매 1분마다 실행
     * - 재시도 횟수가 MAX_RETRY_COUNT 미만인 것만
     */
    @Transactional
    public void retryFailedEvents() {
        List<LikeEventDLQ> retryableEvents = dlqRepository.findRetryableEvents(MAX_RETRY_COUNT)
                .stream()
                .limit(RETRY_BATCH_SIZE)
                .toList();

        if (retryableEvents.isEmpty()) {
            return;
        }

        Map<Long, Long> postLikeDeltaMap = new HashMap<>();

        int successCount = 0;
        int failureCount = 0;

        for  (LikeEventDLQ dlqEvent : retryableEvents) {
            try {
                // DLQ 이벤트를 원본 이벤트로 변환
                LikePostEvent originalEvent = LikePostEvent.builder()
                        .seq(dlqEvent.getOriginalEventSeq())
                        .memberSeq(dlqEvent.getMemberSeq())
                        .postSeq(dlqEvent.getPostSeq())
                        .actionType(dlqEvent.getActionType())
                        .build();

                // 재처리 시도
                boolean statusChanged = likeEventProcessor.processSingleLikeStatus(originalEvent);

                if (statusChanged) {
                    long delta = (dlqEvent.getActionType() == ActionType.LIKE) ? 1L : -1L;
                    postLikeDeltaMap.merge(dlqEvent.getPostSeq(), delta, Long::sum);
                    //성공하면 DLQ에서 삭제
                    dlqRepository.delete(dlqEvent);
                    successCount++;
                } else {
                    // 중복 이벤트면 DLQ에서 삭제 BUT. 현재 아키텍처에서는 해당 코드로 진입하지 않음, 이벤트 메시징 기반을 염두한 코드
                    dlqRepository.delete(dlqEvent);
                }
            } catch (Exception e) {
                failureCount++;
                dlqEvent.incrementRetryCount();
            }
        }
        
        updatePostStatsInBatch(postLikeDeltaMap);
    }

    private void updatePostStatsInBatch(Map<Long, Long> postLikeDeltaMap) {
        if (postLikeDeltaMap.isEmpty()) {
            return;
        }

        List<Long> postSeqs = new ArrayList<>(postLikeDeltaMap.keySet());
        List<PostStats> statsList = postStatsRepository.findAllByPostSeqIn(postSeqs);

        Map<Long, PostStats> statsMap = new HashMap<>();
        for (PostStats stats : statsList) {
            statsMap.put(stats.getPostSeq(), stats);
        }

        for (Map.Entry<Long, Long> entry : postLikeDeltaMap.entrySet()) {
            Long  postSeq = entry.getKey();
            Long delta = entry.getValue();

            PostStats stats = statsMap.get(postSeq);
            if (stats != null) {
                stats.setLikeCount(stats.getLikeCount() + delta);
            } else {
                statsList.add(PostStats.builder()
                        .postSeq(postSeq)
                        .likeCount(Math.max(0, delta))
                        .build());
            }
        }

        postStatsRepository.saveAll(statsList);
    }
}
