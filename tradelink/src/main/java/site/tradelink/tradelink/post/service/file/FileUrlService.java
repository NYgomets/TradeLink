package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.post.response.PreSignedUrlDto;
import site.tradelink.tradelink.post.util.S3KeyGenerator;

@Service
@RequiredArgsConstructor
public class FileUrlService {

    private final PresignedUrlService presignedUrlService;
    private final S3KeyGenerator s3KeyGenerator;





    public String issueDownloadUrl(String s3Key) {
        return presignedUrlService.generateDownloadPresignedUrl(s3Key);
    }
}
