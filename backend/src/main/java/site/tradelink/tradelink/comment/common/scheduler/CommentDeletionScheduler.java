package site.tradelink.tradelink.comment.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.comment.service.CommentTransactionalService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentDeletionScheduler {

    private final CommentTransactionalService transactionalService;
    final int RETENTION_DAYS = 1;

    // 추후 Error 처리 작업
    @Scheduled(cron = "0 0 6 * * *")
    public void cleanupPermanentlyDeletedComments() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        try {
            transactionalService.purgeOldSoftDeletedComments(cutoffDate);
        } catch (Exception e) {
            log.error("댓글 삭제 스케줄러 작업 중 오류 발생");
        }
    }
}
