package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.post.response.PreSignedUrlDto;
import site.tradelink.tradelink.post.common.util.S3KeyGenerator;

@Service
@RequiredArgsConstructor
public class FileUrlService {

    private final PresignedUrlService presignedUrlService;
    private final S3KeyGenerator s3KeyGenerator;


    /**
     * Client가 파일 업로드에 사용할 Pre-signed URL을 생성하여 반환
     * @param originalFilename 사용자가 업로드할 파일의 원본 이름
     * @param contentType 파일의 MIME 타입
     * @return Pre-signed URL과 S3 Key가 담긴 DTO
     */
    public PreSignedUrlDto issueUploadUrl(String originalFilename, String contentType) {

        String s3Key = s3KeyGenerator.generatePostPhotoKey(originalFilename);

        String uploadPresignedUrl = presignedUrlService.generateUploadPresignedUrl(s3Key, contentType);

        return PreSignedUrlDto.builder()
                .preSignedUrl(uploadPresignedUrl)
                .s3Key(s3Key)
                .build();
    }


    public String issueDownloadUrl(String s3Key) {
        return presignedUrlService.generateDownloadPresignedUrl(s3Key);
    }
}
