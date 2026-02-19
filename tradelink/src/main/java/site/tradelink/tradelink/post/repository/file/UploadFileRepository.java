package site.tradelink.tradelink.post.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.tradelink.tradelink.post.common.enums.FileStatus;
import site.tradelink.tradelink.post.entity.UploadFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

    // 스케줄러용 soft-delete 파일 조회
    @Query("""
            SELECT f FROM UploadFile f
            WHERE f.status = :status
            AND f.deletedTime < :cutoffDate
            """)
    List<UploadFile> findByStatusAndDeletedTimeBefore(@Param("status") FileStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);

    // 고아 파일 탐지용: S3 키 목록 중 DB에 존재하는 키만 반환
    @Query("""
            SELECT f.s3Key FROM UploadFile f
            WHERE f.s3Key IN :s3Keys
            """)
    Set<String> findExistingS3Keys(@Param("s3Keys")  Set<String> s3Keys);
}
