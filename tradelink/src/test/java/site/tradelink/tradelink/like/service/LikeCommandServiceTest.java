package site.tradelink.tradelink.like.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.like.common.enums.ActionType;
import site.tradelink.tradelink.like.entity.LikePostEvent;
import site.tradelink.tradelink.like.repository.LikePostEventRepository;
import site.tradelink.tradelink.like.request.LikePostDto;
import site.tradelink.tradelink.like.response.LikePostResponseDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LikeCommandServiceTest {

    @InjectMocks
    private LikeCommandService likeCommandService;

    @Mock
    private LikePostEventRepository likePostEventRepository;

    @Test
    @DisplayName("배치 좋아요 이벤트 저장 → 저장된 이벤트 수만큼 acceptedCount 반환")
    void processBatchLikePostEvents_returnAcceptedCount() {
        // given
        Long memberSeq = 1L;

        LikePostDto.LikeAction action1 = createLikeAction(100L, ActionType.LIKE);
        LikePostDto.LikeAction action2 = createLikeAction(200L, ActionType.UNLIKE);
        LikePostDto request = createLikePostDto(List.of(action1, action2));

        List<LikePostEvent> savedEvents = List.of(
                LikePostEvent.builder().memberSeq(memberSeq).postSeq(100L).actionType(ActionType.LIKE).build(),
                LikePostEvent.builder().memberSeq(memberSeq).postSeq(200L).actionType(ActionType.UNLIKE).build()
        );
        given(likePostEventRepository.saveAll(anyList())).willReturn(savedEvents);

        // when
        LikePostResponseDto response = likeCommandService.processBatchLikePostEvents(memberSeq, request);

        // then
        assertThat(response.getAcceptedCount()).isEqualTo(2);
        verify(likePostEventRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("단건 좋아요 이벤트 저장 → acceptedCount 1 반환")
    void processBatchLikePostEvents_singleAction_returnOne() {
        // given
        Long memberSeq = 1L;

        LikePostDto.LikeAction action = createLikeAction(100L, ActionType.LIKE);
        LikePostDto request = createLikePostDto(List.of(action));

        List<LikePostEvent> savedEvents = List.of(
                LikePostEvent.builder().memberSeq(memberSeq).postSeq(100L).actionType(ActionType.LIKE).build()
        );
        given(likePostEventRepository.saveAll(anyList())).willReturn(savedEvents);

        // when
        LikePostResponseDto response = likeCommandService.processBatchLikePostEvents(memberSeq, request);

        // then
        assertThat(response.getAcceptedCount()).isEqualTo(1);
    }

    // --- 헬퍼 ---

    private LikePostDto createLikePostDto(List<LikePostDto.LikeAction> actions) {
        try {
            LikePostDto dto = new LikePostDto();
            java.lang.reflect.Field field = LikePostDto.class.getDeclaredField("actions");
            field.setAccessible(true);
            field.set(dto, actions);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LikePostDto.LikeAction createLikeAction(Long postSeq, ActionType actionType) {
        try {
            LikePostDto.LikeAction action = new LikePostDto.LikeAction();
            java.lang.reflect.Field postSeqField = LikePostDto.LikeAction.class.getDeclaredField("postSeq");
            postSeqField.setAccessible(true);
            postSeqField.set(action, postSeq);

            java.lang.reflect.Field actionTypeField = LikePostDto.LikeAction.class.getDeclaredField("actionType");
            actionTypeField.setAccessible(true);
            actionTypeField.set(action, actionType);

            return action;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}