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

    // 추후 Error 작업 필요
    @Transactional
    public void savePostPhotoMetadataInTx(String s3Key, Post post) {
        UploadFile uploadFile = UploadFile.builder()
                .saveFileName(s3Key)
                .post(post)
                .build();

        UploadFile saved = uploadFileRepository.save(uploadFile);
    }
}
