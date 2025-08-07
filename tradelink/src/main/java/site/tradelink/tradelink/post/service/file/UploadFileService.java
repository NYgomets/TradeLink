package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.tradelink.tradelink.post.util.S3KeyGenerator;

@Service
@RequiredArgsConstructor
public class UploadFileService {

    private final UploadFileTransactionalService transactionalService;
    private final NcpS3Uploader s3Uploader;
    private final S3KeyGenerator s3KeyGenerator;

    /**
     * S3 이후 DB(Tx) 하도록 트랜잭션 분리
     * - 커넥션 고갈 방지
     * - REDO 영역 최적화
     * - (낮은 확률) 데드락 방지
     */
    public Long savePostPhoto(MultipartFile multipartFile, Long postSeq) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return -1L;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String s3Key = s3KeyGenerator.generatePostPhotoKey(originalFilename);

        // 1) S3 업로드
        s3Uploader.uploadFile(multipartFile, s3Key);

        // 2) DB 저장 (트랜잭션 안) / 추후 Error 처리 작업 필요
        try {
            return transactionalService.savePostPhotoMetadataInTx(originalFilename, s3Key, postSeq);
        } catch (Exception e) {
            s3Uploader.deleteFile(s3Key);
            throw e;
        }
    }
}
