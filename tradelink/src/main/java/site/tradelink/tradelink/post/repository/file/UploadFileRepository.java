package site.tradelink.tradelink.post.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;
import site.tradelink.tradelink.post.entity.UploadFile;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {

}
