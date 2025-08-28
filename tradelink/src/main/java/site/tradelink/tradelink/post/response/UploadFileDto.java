package site.tradelink.tradelink.post.response;

import lombok.Getter;
import lombok.Setter;
import site.tradelink.tradelink.post.entity.UploadFile;

@Getter
@Setter
public class UploadFileDto {
    private Long seq;
    private String savedFileName;

    public UploadFileDto(UploadFile uploadFile) {
        this.seq = uploadFile.getSeq();
        this.savedFileName = uploadFile.getS3Key();
    }
}
