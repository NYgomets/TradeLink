package site.tradelink.tradelink.like.service.failed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.failed.LikeEventDLQ;
import site.tradelink.tradelink.like.repository.failed.LikeEventDLQRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeEventDLQServiceTest {

    @InjectMocks
    private LikeEventDLQService likeEventDLQService;

    @Mock
    private LikeEventDLQRepository likeEventDLQRepository;

    @Nested
    @DisplayName("moveToDLQ()")
    class MoveToDLQ {

        @Test
        @DisplayName("DLQ에 없는 이벤트 → 새로운 DLQ 엔트리 생성 (retryCount=1)")
        void moveToDLQ_whenNotExists_createNewEntry() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(event, 10L);

            given(likeEventDLQRepository.findByOriginalEventSeq(10L))
                    .willReturn(Optional.empty());

            ArgumentCaptor<LikeEventDLQ> captor = ArgumentCaptor.forClass(LikeEventDLQ.class);

            // when
            likeEventDLQService.moveToDLQ(event);

            // then
            verify(likeEventDLQRepository).save(captor.capture());
            LikeEventDLQ saved = captor.getValue();
            assertThat(saved.getOriginalEventSeq()).isEqualTo(10L);
            assertThat(saved.getMemberSeq()).isEqualTo(1L);
            assertThat(saved.getPostSeq()).isEqualTo(100L);
            assertThat(saved.getActionType()).isEqualTo(ActionType.LIKE);
            assertThat(saved.getRetryCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("DLQ에 이미 존재하는 이벤트 → retryCount 증가 (새로 저장 안 함)")
        void moveToDLQ_whenAlreadyExists_incrementRetryCount() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();
            setSeq(event, 10L);

            LikeEventDLQ existingDLQ = LikeEventDLQ.builder()
                    .originalEventSeq(10L)
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .retryCount(1)
                    .build();

            given(likeEventDLQRepository.findByOriginalEventSeq(10L))
                    .willReturn(Optional.of(existingDLQ));

            // when
            likeEventDLQService.moveToDLQ(event);

            // then
            verify(likeEventDLQRepository, never()).save(any());
            assertThat(existingDLQ.getRetryCount()).isEqualTo(2);
        }
    }

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
