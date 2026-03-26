package site.tradelink.tradelink.post.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.post.common.enums.FileStatus;
import site.tradelink.tradelink.post.common.enums.PostStatus;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.repository.file.UploadFileRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileDeletionTransactionalService {

    private final UploadFileRepository uploadFileRepository;
    private final PostRepository postRepository;

    final int RETENTION_DAYS = 1;

    @Transactional
    public List<String> findAndPurgeSoftDeletedPosts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<Post> postsToPurge = postRepository.findByStatusAndDeletedTimeBefore(PostStatus.DELETED, cutoffDate);

        if (postsToPurge.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> postSeqs = postsToPurge.stream()
                .map(Post::getSeq)
                .toList();

        // batch_fetch_size 설정으로 자동으로 IN 쿼리 실행
        List<String> s3KeysToPurge = postsToPurge.stream()
                .flatMap(post -> post.getUploadFiles().stream())
                .map(UploadFile::getS3Key)
                .toList();

        // UploadFile 먼저 IN 쿼리로 삭제 → orphanRemoval 동작 안 하도록 선제 처리
        uploadFileRepository.deleteByPostSeqIn(postSeqs);

        // Post 배치 삭제
        postRepository.deleteAllInBatch(postsToPurge);

        return s3KeysToPurge;
    }

    @Transactional
    public List<String> findAndPurgeSoftDeletedIndividualFiles() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<UploadFile> filesToPurge = uploadFileRepository.findByStatusAndDeletedTimeBefore(FileStatus.DELETED, cutoffDate);

        if (filesToPurge.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> s3KeysToPurge = filesToPurge.stream()
                .map(UploadFile::getS3Key)
                .toList();

        uploadFileRepository.deleteAllInBatch(filesToPurge);

        return s3KeysToPurge;
    }

    /**
     * S3에서 조회한 키 목록(한 페이지 분량)을 받아 DB에 존재하는 키만 필터링하여 반환
     */
    @Transactional(readOnly = true)
    public Set<String> filterExistingKeys(Set<String> s3KeysFromPage) {
        if (s3KeysFromPage == null || s3KeysFromPage.isEmpty()) {
            return Collections.emptySet();
        }

        return uploadFileRepository.findExistingS3Keys(s3KeysFromPage);
    }
}
