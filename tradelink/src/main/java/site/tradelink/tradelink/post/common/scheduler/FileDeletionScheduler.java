package site.tradelink.tradelink.post.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.post.common.event.S3DeletionFailureEvent;
import site.tradelink.tradelink.post.common.exception.S3DeletionException;
import site.tradelink.tradelink.post.service.file.NcpS3Service;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileDeletionScheduler {

    private final NcpS3Service ncpS3Service;
    private final FileDeletionTransactionalService fileDeletionTransactionalService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${cloud.aws.s3.path.postPhoto}")
    private String postPhotoPath;

    private static final int S3_BULK_DELETE_LIMIT = 1000;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupSoftDeletedPostsAndFiles() {
        List<String> s3KeysToPurge = fileDeletionTransactionalService.findAndPurgeSoftDeletedPosts();

        if (!s3KeysToPurge.isEmpty()) {
            try {
                ncpS3Service.deleteFiles(s3KeysToPurge);
            } catch (S3DeletionException e) {
                eventPublisher.publishEvent(new S3DeletionFailureEvent(e.getFailures()));
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupSoftDeletedIndividualFiles() {
        List<String> s3KeysToPurge = fileDeletionTransactionalService.findAndPurgeSoftDeletedIndividualFiles();

        if (!s3KeysToPurge.isEmpty()) {
            try {
                ncpS3Service.deleteFiles(s3KeysToPurge);
            } catch (S3DeletionException e) {
                eventPublisher.publishEvent(new S3DeletionFailureEvent(e.getFailures()));
            }
        }
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupOrphanedS3Files() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);

        ListObjectsV2Iterable paginatedObjects = ncpS3Service.listObjectsByPrefixPaginated(postPhotoPath);

        paginatedObjects.forEach(page -> {
            // 1. 현재 페이지에서 유예 기간이 지난 파일 키만 필터링
            Set<String> s3KeyInPage = page.contents().stream()
                    .filter(s3Object -> s3Object.lastModified().isBefore(cutoff))
                    .map(S3Object::key)
                    .collect(Collectors.toSet());

            if (s3KeyInPage.isEmpty()) {
                return;
            }

            // 2. DB에 존재하는 키들을 조회
            Set<String> existingDbKeys = fileDeletionTransactionalService.filterExistingKeys(s3KeyInPage);

            // 3. 고아 파일 확정
            s3KeyInPage.removeAll(existingDbKeys);

            // 4. 고아 파일이 있다면, 서버 메모리에 쌓아두지 않고 즉시 삭제
            if (!s3KeyInPage.isEmpty()) {
                List<String> orphanKeysInPage = new ArrayList<>(s3KeyInPage);

                // S3 벌크 삭제 제한(1000개)에 맞춰 분할 삭제
                for (int i=0; i< orphanKeysInPage.size(); i += S3_BULK_DELETE_LIMIT) {
                    List<String> subList = orphanKeysInPage.subList(i, Math.min(i + S3_BULK_DELETE_LIMIT, orphanKeysInPage.size()));

                    try {
                        ncpS3Service.deleteFiles(subList);
                    } catch (S3DeletionException e) {
                        eventPublisher.publishEvent(new S3DeletionFailureEvent(e.getFailures()));
                    }
                }
            }
        });
    }
}
