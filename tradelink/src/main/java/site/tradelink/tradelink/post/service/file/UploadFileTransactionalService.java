package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.post.entity.Post;
import site.tradelink.tradelink.post.entity.UploadFile;
import site.tradelink.tradelink.post.repository.PostRepository;
import site.tradelink.tradelink.post.repository.file.UploadFileRepository;

@Service
@RequiredArgsConstructor
public class UploadFileTransactionalService {

    private final UploadFileRepository uploadFileRepository;
    private final PostRepository postRepository;
    private final NcpS3Uploader s3Uploader;

    // 추후 Error 작업 필요
    @Transactional
    public Long savePostPhotoMetadataInTx(String originalFilename, String s3Key, Long postSeq) {
        Post post = postRepository.findById(postSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));

        UploadFile uploadFile = UploadFile.builder()
                .originalFileName(originalFilename)
                .saveFileName(s3Key)
                .url(s3Uploader.generateUrl(s3Key))
                .post(post)
                .build();

        UploadFile saved = uploadFileRepository.save(uploadFile);
        return saved.getSeq();
    }
}
