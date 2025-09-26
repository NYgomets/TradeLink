package site.tradelink.tradelink.post.common.eventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.post.common.event.S3DeletionFailureEvent;
import site.tradelink.tradelink.post.common.exception.S3DeletionException;
import site.tradelink.tradelink.post.service.file.NcpS3Service;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3DeletionFailureEventListener {

    private final NcpS3Service ncpS3Service;

    @Async
    @EventListener
    @Retryable(
            retryFor = {S3DeletionException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 3000) // 3초 간격
    )
    public void handleS3DeletionFailure(S3DeletionFailureEvent event) {
        ArrayList<String> keysToRetry = new ArrayList<>(event.getFailedKeysWithReason().keySet());

        ncpS3Service.deleteFiles(keysToRetry);
    }

    @Recover
    public void notifyDeletionFailure(S3DeletionException e) {
        log.error("S3 파일 삭제 재시도에 최종 실패했습니다. 수동 확인 필요. 실패 목록: {}", e.getFailures());
    }
}