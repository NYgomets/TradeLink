package site.tradelink.tradelink.comment.common.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.comment.service.CommentTransactionalService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentDeletionScheduler 단위 테스트")
class CommentDeletionSchedulerTest {

    @InjectMocks
    private CommentDeletionScheduler scheduler;

    @Mock
    private CommentTransactionalService transactionalService;

    @Test
    @DisplayName("cutoffDate는 현재 시각에서 RETENTION_DAYS를 뺀 값으로 전달된다")
    void passesCutoffDateAsNowMinusRetentionDaysToPurgeService() {
        // given
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        LocalDateTime beforeCall = LocalDateTime.now().minusDays(scheduler.RETENTION_DAYS);

        // when
        scheduler.cleanupPermanentlyDeletedComments();

        // then
        verify(transactionalService).purgeOldSoftDeletedComments(captor.capture());
        LocalDateTime captured = captor.getValue();
        LocalDateTime afterCall = LocalDateTime.now().minusDays(scheduler.RETENTION_DAYS);

        assertThat(captured).isBetween(beforeCall, afterCall);
    }

    @Test
    @DisplayName("purge 서비스에서 예외가 발생해도 외부로 전파되지 않는다")
    void doesNotPropagateExceptionWhenPurgeServiceThrows() {
        // given
        doThrow(new RuntimeException("DB error"))
                .when(transactionalService).purgeOldSoftDeletedComments(any());

        // when & then - 예외가 외부로 전파되지 않아야 함
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> scheduler.cleanupPermanentlyDeletedComments()
        );
    }

    @Test
    @DisplayName("스케줄러 실행 시 purge 서비스는 정확히 1번 호출된다")
    void callsPurgeServiceExactlyOncePerExecution() {
        // when
        scheduler.cleanupPermanentlyDeletedComments();

        // then
        verify(transactionalService, times(1)).purgeOldSoftDeletedComments(any());
    }
}