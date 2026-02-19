package site.tradelink.tradelink.like.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.entity.LikeStatus;
import site.tradelink.tradelink.like.repository.LikeStatusRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeEventProcessorTest {

    @InjectMocks
    private LikeEventProcessor likeEventProcessor;

    @Mock
    private LikeStatusRepository likeStatusRepository;

    @Nested
    @DisplayName("processSingleLikeStatus()")
    class ProcessSingleLikeStatus {

        @Test
        @DisplayName("좋아요 상태가 없을 때 LIKE 이벤트 처리 → LikeStatus 새로 생성 후 true 반환")
        void like_whenNoExistingStatus_createAndReturnTrue() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();

            LikeStatus newStatus = LikeStatus.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .isLiked(false)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(1L, 100L))
                    .willReturn(Optional.empty());
            given(likeStatusRepository.save(any(LikeStatus.class)))
                    .willReturn(newStatus);

            // when
            boolean result = likeEventProcessor.processSingleLikeStatus(event);

            // then
            assertThat(result).isTrue();
            assertThat(newStatus.getIsLiked()).isTrue();
        }

        @Test
        @DisplayName("이미 좋아요 상태일 때 LIKE 이벤트 처리 → 중복이므로 false 반환")
        void like_whenAlreadyLiked_returnFalse() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.LIKE)
                    .build();

            LikeStatus existingStatus = LikeStatus.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .isLiked(true)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(1L, 100L))
                    .willReturn(Optional.of(existingStatus));

            // when
            boolean result = likeEventProcessor.processSingleLikeStatus(event);

            // then
            assertThat(result).isFalse();
            verify(likeStatusRepository, never()).save(any());
        }

        @Test
        @DisplayName("좋아요 상태일 때 UNLIKE 이벤트 처리 → 상태 변경 후 true 반환")
        void unlike_whenCurrentlyLiked_returnTrue() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.UNLIKE)
                    .build();

            LikeStatus existingStatus = LikeStatus.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .isLiked(true)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(1L, 100L))
                    .willReturn(Optional.of(existingStatus));

            // when
            boolean result = likeEventProcessor.processSingleLikeStatus(event);

            // then
            assertThat(result).isTrue();
            assertThat(existingStatus.getIsLiked()).isFalse();
        }

        @Test
        @DisplayName("좋아요 안한 상태일 때 UNLIKE 이벤트 처리 → 중복이므로 false 반환")
        void unlike_whenAlreadyUnliked_returnFalse() {
            // given
            LikePostEvent event = LikePostEvent.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .actionType(ActionType.UNLIKE)
                    .build();

            LikeStatus existingStatus = LikeStatus.builder()
                    .memberSeq(1L)
                    .postSeq(100L)
                    .isLiked(false)
                    .build();

            given(likeStatusRepository.findByMemberSeqAndPostSeq(1L, 100L))
                    .willReturn(Optional.of(existingStatus));

            // when
            boolean result = likeEventProcessor.processSingleLikeStatus(event);

            // then
            assertThat(result).isFalse();
            verify(likeStatusRepository, never()).save(any());
        }
    }
}