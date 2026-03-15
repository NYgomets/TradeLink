package site.tradelink.tradelink.like.common.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.PostStats;
import site.tradelink.tradelink.like.entity.ProcessorOffset;
import site.tradelink.tradelink.like.repository.LikePostEventRepository;
import site.tradelink.tradelink.like.repository.PostStatsRepository;
import site.tradelink.tradelink.like.repository.ProcessorOffsetRepository;
import site.tradelink.tradelink.like.service.LikeEventProcessor;
import site.tradelink.tradelink.like.service.failed.LikeEventDLQService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessLikeEventsTest {

    @InjectMocks
    private ProcessLikeEvents processLikeEvents;

    @Mock
    private LikeEventDLQService likeEventDLQService;

    @Mock
    private LikeEventProcessor likeEventProcessor;

    @Mock
    private LikePostEventRepository likePostEventRepository;

    @Mock
    private PostStatsRepository postStatsRepository;

    @Mock
    private ProcessorOffsetRepository processorOffsetRepository;

    @Nested
    @DisplayName("processLikeEvents()")
    class ProcessLikeEventsMethod {

        @Test
        @DisplayName("처리할 이벤트가 없으면 아무것도 실행하지 않는다")
        void processLikeEvents_whenNoEvents_doNothing() {
            // given
            ProcessorOffset offset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.of(offset));
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of());

            // when
            processLikeEvents.processLikeEvents();

            // then
            verify(likeEventProcessor, never()).processSingleLikeStatus(any());
            verify(postStatsRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("LIKE 이벤트 처리 성공 → PostStats likeCount +1, offset 업데이트")
        void processLikeEvents_withLikeEvent_incrementLikeCount() {
            // given
            ProcessorOffset offset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(event, 1L);

            PostStats postStats = PostStats.builder()
                    .postSeq(100L)
                    .likeCount(5L)
                    .build();

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.of(offset));
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of(event));
            given(likeEventProcessor.processSingleLikeStatus(event))
                    .willReturn(true);
            given(postStatsRepository.findAllByPostSeqIn(List.of(100L)))
                    .willReturn(List.of(postStats));

            // when
            processLikeEvents.processLikeEvents();

            // then
            assertThat(postStats.getLikeCount()).isEqualTo(6L);
            assertThat(offset.getLastProcessedEventSeq()).isEqualTo(1L);
            verify(postStatsRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("UNLIKE 이벤트 처리 성공 → PostStats likeCount -1")
        void processLikeEvents_withUnlikeEvent_decrementLikeCount() {
            // given
            ProcessorOffset offset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.UNLIKE)
                    .build();
            setSeq(event, 2L);

            PostStats postStats = PostStats.builder()
                    .postSeq(100L)
                    .likeCount(5L)
                    .build();

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.of(offset));
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of(event));
            given(likeEventProcessor.processSingleLikeStatus(event))
                    .willReturn(true);
            given(postStatsRepository.findAllByPostSeqIn(List.of(100L)))
                    .willReturn(List.of(postStats));

            // when
            processLikeEvents.processLikeEvents();

            // then
            assertThat(postStats.getLikeCount()).isEqualTo(4L);
        }

        @Test
        @DisplayName("이벤트 처리 중 예외 발생 → DLQ 이동, offset은 계속 진행 (Poison Pill 방지)")
        void processLikeEvents_whenExceptionOccurs_moveToDLQAndContinue() {
            // given
            ProcessorOffset offset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            LikePostEvent failEvent = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(failEvent, 1L);

            LikePostEvent successEvent = LikePostEvent.builder()
                    .memberSeq(2L)
                    .postSeq(200L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(successEvent, 2L);

            PostStats postStats = PostStats.builder()
                    .postSeq(200L)
                    .likeCount(0L)
                    .build();

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.of(offset));
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of(failEvent, successEvent));
            given(likeEventProcessor.processSingleLikeStatus(failEvent))
                    .willThrow(new RuntimeException("처리 실패"));
            given(likeEventProcessor.processSingleLikeStatus(successEvent))
                    .willReturn(true);
            given(postStatsRepository.findAllByPostSeqIn(anyList()))
                    .willReturn(List.of(postStats));

            // when
            processLikeEvents.processLikeEvents();

            // then
            verify(likeEventDLQService).moveToDLQ(failEvent);         // 실패 이벤트 → DLQ
            verify(likeEventProcessor).processSingleLikeStatus(successEvent); // 성공 이벤트는 계속 처리
            assertThat(offset.getLastProcessedEventSeq()).isEqualTo(2L);      // offset은 끝까지 이동
        }

        @Test
        @DisplayName("offset이 없으면 0으로 초기화 후 생성")
        void processLikeEvents_whenNoOffset_initializeWithZero() {
            // given
            ProcessorOffset newOffset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.empty());
            given(processorOffsetRepository.save(any(ProcessorOffset.class)))
                    .willReturn(newOffset);
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of());

            // when
            processLikeEvents.processLikeEvents();

            // then
            verify(processorOffsetRepository).save(any(ProcessorOffset.class));
        }

        @Test
        @DisplayName("PostStats가 없는 게시글 → 새 PostStats 생성 후 저장")
        void processLikeEvents_whenNoPostStats_createNewStats() {
            // given
            ProcessorOffset offset = ProcessorOffset.builder()
                    .processorName("LIKE_POST_PROCESSOR")
                    .lastProcessedEventSeq(0L)
                    .build();

            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(999L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(event, 1L);

            given(processorOffsetRepository.findByProcessorName("LIKE_POST_PROCESSOR"))
                    .willReturn(Optional.of(offset));
            given(likePostEventRepository.findEventsAfterCursor(eq(0L), any(PageRequest.class)))
                    .willReturn(List.of(event));
            given(likeEventProcessor.processSingleLikeStatus(event))
                    .willReturn(true);
            given(postStatsRepository.findAllByPostSeqIn(List.of(999L)))
                    .willReturn(List.of()); // 기존 stats 없음

            ArgumentCaptor<List<PostStats>> captor = ArgumentCaptor.forClass(List.class);

            // when
            processLikeEvents.processLikeEvents();

            // then
            verify(postStatsRepository).saveAll(captor.capture());
            List<PostStats> saved = captor.getValue();
            assertThat(saved).hasSize(1);
            assertThat(saved.get(0).getPostSeq()).isEqualTo(999L);
            assertThat(saved.get(0).getLikeCount()).isEqualTo(1L);
        }
    }

    // --- 헬퍼: @Id 필드 reflection으로 세팅 ---
    private void setSeq(LikePostEvent event, Long seq) {
        try {
            java.lang.reflect.Field field = LikePostEvent.class.getDeclaredField("seq");
            field.setAccessible(true);
            field.set(event, seq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}