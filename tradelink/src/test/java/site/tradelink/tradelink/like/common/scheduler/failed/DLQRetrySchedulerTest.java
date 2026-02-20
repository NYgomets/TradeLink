package site.tradelink.tradelink.like.common.scheduler.failed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.failed.LikeEventDLQ;
import site.tradelink.tradelink.like.repository.failed.LikeEventDLQRepository;
import site.tradelink.tradelink.like.service.LikeEventProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DLQRetrySchedulerTest {

    @InjectMocks
    private DLQRetryScheduler dlqRetryScheduler;

    @Mock
    private LikeEventProcessor likeEventProcessor;

    @Mock
    private LikeEventDLQRepository dlqRepository;

    @Nested
    @DisplayName("retryFailedEvents()")
    class RetryFailedEvents {

        @Test
        @DisplayName("재처리할 이벤트가 없으면 아무것도 실행하지 않는다")
        void retryFailedEvents_whenEmpty_doNothing() {
            // given
            given(dlqRepository.findRetryableEvents(3)).willReturn(List.of());

            // when
            dlqRetryScheduler.retryFailedEvents();

            // then
            verify(likeEventProcessor, never()).processSingleLikeStatus(any());
        }

        @Test
        @DisplayName("재처리 성공 (statusChanged=true) → DLQ에서 삭제")
        void retryFailedEvents_whenSuccess_deleteFromDLQ() {
            // given
            LikeEventDLQ dlqEvent = buildDLQEvent(1L, 10L, 100L, ActionType.LIKE, 1);

            given(dlqRepository.findRetryableEvents(3)).willReturn(List.of(dlqEvent));
            given(likeEventProcessor.processSingleLikeStatus(any())).willReturn(true);

            // when
            dlqRetryScheduler.retryFailedEvents();

            // then
            verify(dlqRepository).delete(dlqEvent);
        }

        @Test
        @DisplayName("재처리 중 예외 발생 → retryCount 증가, DLQ 유지")
        void retryFailedEvents_whenException_incrementRetryCount() {
            // given
            LikeEventDLQ dlqEvent = buildDLQEvent(1L, 10L, 100L, ActionType.LIKE, 1);

            given(dlqRepository.findRetryableEvents(3)).willReturn(List.of(dlqEvent));
            given(likeEventProcessor.processSingleLikeStatus(any()))
                    .willThrow(new RuntimeException("재처리 실패"));

            // when
            dlqRetryScheduler.retryFailedEvents();

            // then
            verify(dlqRepository, never()).delete(any()); // 삭제 안 함
            assertThat(dlqEvent.getRetryCount()).isEqualTo(2); // 1 → 2
        }

        @Test
        @DisplayName("일부 성공 일부 실패 → 성공한 것만 삭제, 실패한 것은 retryCount 증가")
        void retryFailedEvents_mixedResult_handleEachIndependently() {
            // given
            LikeEventDLQ successEvent = buildDLQEvent(1L, 10L, 100L, ActionType.LIKE, 1);
            LikeEventDLQ failEvent = buildDLQEvent(2L, 20L, 200L, ActionType.UNLIKE, 2);

            given(dlqRepository.findRetryableEvents(3))
                    .willReturn(List.of(successEvent, failEvent));
            given(likeEventProcessor.processSingleLikeStatus(argThat(e -> e.getMemberSeq().equals(10L))))
                    .willReturn(true);
            given(likeEventProcessor.processSingleLikeStatus(argThat(e -> e.getMemberSeq().equals(20L))))
                    .willThrow(new RuntimeException("실패"));

            // when
            dlqRetryScheduler.retryFailedEvents();

            // then
            verify(dlqRepository).delete(successEvent);
            verify(dlqRepository, never()).delete(failEvent);
            assertThat(failEvent.getRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("재처리 배치 사이즈(10)를 초과하는 이벤트는 이번 실행에서 처리하지 않는다")
        void retryFailedEvents_respectBatchSize() {
            // given - 15개의 DLQ 이벤트
            List<LikeEventDLQ> manyEvents = buildDLQEventList(15);
            given(dlqRepository.findRetryableEvents(3)).willReturn(manyEvents);
            given(likeEventProcessor.processSingleLikeStatus(any())).willReturn(true);

            // when
            dlqRetryScheduler.retryFailedEvents();

            // then - 최대 10개만 처리
            verify(likeEventProcessor, times(10)).processSingleLikeStatus(any());
        }
    }

    // --- 헬퍼 ---

    private LikeEventDLQ buildDLQEvent(Long seq, Long memberSeq, Long postSeq, ActionType actionType, int retryCount) {
        LikeEventDLQ dlq = LikeEventDLQ.builder()
                .originalEventSeq(seq)
                .memberSeq(memberSeq)
                .postSeq(postSeq)
                .actionType(actionType)
                .retryCount(retryCount)
                .build();
        setSeq(dlq, seq);
        return dlq;
    }

    private List<LikeEventDLQ> buildDLQEventList(int count) {
        return java.util.stream.LongStream.rangeClosed(1, count)
                .mapToObj(i -> buildDLQEvent(i, i * 10, i * 100, ActionType.LIKE, 1))
                .toList();
    }

    private void setSeq(LikeEventDLQ dlq, Long seq) {
        try {
            java.lang.reflect.Field field = LikeEventDLQ.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(dlq, seq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
