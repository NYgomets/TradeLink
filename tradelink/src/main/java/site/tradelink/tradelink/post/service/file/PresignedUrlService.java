package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner s3Presigner;

    @Value("${cloud.ncp.s3.bucket}")
    private String bucket;

    @Value("${cloud.ncp.s3.presign.upload-expire-seconds}")
    private long uploadExpireSeconds;

    @Value("${cloud.ncp.s3.presign.download-expire-seconds}")
    private long downloadExpireSeconds;

    /**
     * Client가 S3에 직접 업로드 할 수 있는 Pre-signed URL 생성
     * @param s3Key S3에 저장될 파일의 고유 키
     * @param contentType 업로드할 파일의 MIME 타입
     * @return 생성된 업로드용 Pre-signed URL
     */
    public String generateUploadPresignedUrl(String s3Key, String contentType) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(uploadExpireSeconds))
                .putObjectRequest(putReq)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);

        return presigned.url().toString();
    }

    /**
     * Client가 비공개 파일을 다운로드 할 수 있는 Pre-signed URL 생성
     * @param s3Key 조회할 파일의 고유 키
     * @return 생성된 다운로드용 Pre-signed URL
     */
    public String generateDownloadPresignedUrl(String s3Key) {
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(downloadExpireSeconds))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);

        return presigned.url().toString();
    }
}
