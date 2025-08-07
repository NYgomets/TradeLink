package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class NcpS3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.ncp.s3.bucket")
    private String bucket;

    /**
     * File.createTempFile() -> 디스크 I/O 병목 가능성으로 인하여 변경
     * 1. MultipartFile을 S3에 업로드하기 위해 임시파일 생성
     * 2. MultipartFile.transferTo(tempFile)로 실제 파일을 디스크에 쓴 뒤
     * 3. S3로 파일 전송(PutObject)
     * 문제점:
     * - 운영환경에서 다수의 업로드가 동시 발생할 경우, 디스크 I/O 병목 및 임시 파일 누적 가능성 존재
     * 대안:
     * - RequestBody.fromInputStream()
     */
    public void uploadFile(MultipartFile multipartFile, String s3Key) {
        try (InputStream inputStream = multipartFile.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, multipartFile.getSize()));
        } catch (IOException e) {
            // 추후 Error 작업 추가
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    public void deleteFile(String s3Key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        s3Client.deleteObject(request);
    }

    public String generateUrl(String s3Key) {
        return "https://" + bucket + ".kr.object.ncloudstorage.com/" + s3Key;
    }
}
