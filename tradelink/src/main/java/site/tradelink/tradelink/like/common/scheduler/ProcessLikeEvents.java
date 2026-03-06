package site.tradelink.tradelink.like.common.scheduler;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.entity.ProcessorOffset;
import site.tradelink.tradelink.like.repository.LikePostEventRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.repository.ProcessorOffsetRepository;
import site.tradelink.tradelink.like.service.LikeEventProcessor;
import site.tradelink.tradelink.like.service.failed.LikeEventDLQService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessLikeEvents {

    private final LikeEventDLQService likeEventDLQService;
    private final LikeEventProcessor likeEventProcessor;

    private final LikePostEventRepository likePostEventRepository;
    private final PostStatsRepository postStatsRepository;
    private final ProcessorOffsetRepository processorOffsetRepository;

    private static final String PROCESSOR_KEY = "LIKE_POST_PROCESSOR";
    private static final int BATCH_SIZE = 100;

    @Transactional
    public void processLikeEvents() {
        ProcessorOffset offset = getOrInitOffset();
        Long lastSeq = offset.getLastProcessedEventSeq();

        List<LikePostEvent> events = likePostEventRepository.findEventsAfterCursor(
                lastSeq, PageRequest.of(0, BATCH_SIZE));

        if (events.isEmpty()) {
            return;
        }

        BatchProcessResult result = processBatchWithDLQ(events);

        // memory에서 각 post의 좋아요 count 계산을 끝마친 후 DB Update
        updatePostStatsInBatch(result.postLikeDeltaMap);

        offset.updateOffset(result.lastProcessedSeq);
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
            Long postSeq = entry.getKey();
            Long delta = entry.getValue();

            PostStats stats = statsMap.get(postSeq);
            if (stats != null) {
                stats.setLikeCount(stats.getLikeCount() + delta);
            } else {
                PostStats newStats = PostStats.builder()
                        .postSeq(postSeq)
                        .likeCount(Math.max(0, delta))
                        .build();

                statsList.add(newStats);
            }
        }

        postStatsRepository.saveAll(statsList);
    }

    /**
     * 배치 처리 with DLQ
     * - 실패한 이벤트는 DLQ로 이동
     * - 나머지는 계속 진행
     */
    private BatchProcessResult processBatchWithDLQ(List<LikePostEvent> events) {
        Map<Long, Long> postLikeDeltaMap = new HashMap<>();
        long lastProcessedSeq = events.getFirst().getSeq() - 1;
        int successCount = 0;
        int skippedCount = 0; // 현재의 DB 기반 이벤트 로그에서는 불필요함 BUT. 이벤트 메시징(Kafka, RabbitMQ, SQS 등을 사용할 경우 필요해짐. 코드 확장성에 따른 변수 추가.
        int failedCount = 0;

        for (LikePostEvent event : events) {
            try {
                boolean statusChanged = likeEventProcessor.processSingleLikeStatus(event);

                if (statusChanged) {
                    long delta = (event.getActionType() == ActionType.LIKE) ? 1L : -1L;
                    postLikeDeltaMap.merge(event.getPostSeq(), delta, Long::sum);
                    successCount++;
                } else {
                    skippedCount++;
                }

                // 성공하면 offset 이동
                lastProcessedSeq = event.getSeq();

            } catch (Exception e) {
                failedCount++;

                // DLQ로 이동 (별도 트랜잭션)
                likeEventDLQService.moveToDLQ(event);

                // 실패해도 offset 이동 *(Poison Pill 방지)*
                lastProcessedSeq = event.getSeq();
            }
        }

        return BatchProcessResult.builder()
                .postLikeDeltaMap(postLikeDeltaMap)
                .lastProcessedSeq(lastProcessedSeq)
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(failedCount)
                .build();
    }

    private ProcessorOffset getOrInitOffset() {
        return processorOffsetRepository.findByProcessorName(PROCESSOR_KEY)
                .orElseGet(() -> processorOffsetRepository.save(
                        ProcessorOffset.builder()
                                .processorName(PROCESSOR_KEY)
                                .lastProcessedEventSeq(0L)
                                .build()));
    }

    @Builder
    private static class BatchProcessResult {
        Map<Long, Long> postLikeDeltaMap;
        Long lastProcessedSeq;
        int successCount;
        int skippedCount;
        int failedCount;
    }
}
