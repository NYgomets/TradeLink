package site.tradelink.tradelink.post.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.common.enums.FileStatus;
import site.tradelink.tradelink.post.entity.UploadFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    List<UploadFile> findByStatusAndDeletedTimeBefore(FileStatus status, LocalDateTime cutoffDate);

    Set<String> findExistingS3Keys(Set<String> s3Keys);
}
