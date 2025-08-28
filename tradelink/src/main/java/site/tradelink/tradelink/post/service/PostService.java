package site.tradelink.tradelink.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.post.common.enums.AllowedImageContentType;
import site.tradelink.tradelink.post.request.PostCreateDto;
import site.tradelink.tradelink.post.response.PreSignedUrlDto;
import site.tradelink.tradelink.post.service.file.FileUrlService;
import site.tradelink.tradelink.post.service.file.NcpS3Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostTransactionalService transactionalService;
    private final FileUrlService fileUrlService;
    private final NcpS3Service ncpS3Service;

    /**
     * [1단계] 게시글 파일 업로드를 위한 Pre-signed URL을 발급
     * 클라이언트는 이 URL로 파일을 S3에 업로드한 후,
     * 반환된 s3Key을 포함하여 게시글 생성 요청을 보내야 함.
     */
    public PreSignedUrlDto issueUploadUrl(String originalFilename, String contentType) {

        // 추후 Error 작업 추가 필요
        if (!AllowedImageContentType.isAllowed(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 파일 형식입니다.");
        }

        return fileUrlService.issueUploadUrl(originalFilename, contentType);
    }

    /**
     * [2단계] Client가 S3에 파일 업로드를 완료한 후, 게시글 생성을 최종 확정
     * Controller로부터 인증된 사용자의 Seq(memberSeq)를 받아 사용 (세션에 저장되어 있음)
     */
    public void createPost(PostCreateDto request, Long memberSeq) {
        transactionalService.createPostAndMetadata(request, memberSeq);
    }

    public void deletePost(Long postSeq, Long memberSeq) {
        List<String> s3KeysToDelete = transactionalService.deletePostAndGetS3Keys(postSeq, memberSeq);

        if (s3KeysToDelete != null && !s3KeysToDelete.isEmpty()) {
            ncpS3Service.deleteFiles(s3KeysToDelete);
        }
    }
}
