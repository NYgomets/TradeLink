package site.tradelink.tradelink.post.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.post.repository.file.UploadFileRepository;
import site.tradelink.tradelink.post.service.file.NcpS3Service;

@Component
@RequiredArgsConstructor
public class FileDeletionScheduler {

    private final UploadFileRepository uploadFileRepository;
    private final NcpS3Service ncpS3Service;
}
