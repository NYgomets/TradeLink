package site.tradelink.tradelink.post.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class S3KeyGenerator {

     @Value("${cloud.ncp.s3.path.postPhoto}")
    private String postPhotoPath;

     public String generatePostPhotoKey(String originalFilename) {
         String ext = extractExt(originalFilename);
         String uuid = UUID.randomUUID().toString();
         return postPhotoPath + "/" + uuid + "." + ext;
     }

    private String extractExt(String fileName) {
         int pos = fileName.lastIndexOf('.');
         if (pos == -1 || pos == fileName.length()-1) {
             // 추후 Error 문구 수정
             throw  new IllegalArgumentException("파일 확장자를 찾을 수 없습니다.");
         }

         return fileName.substring(pos+1).toLowerCase();
    }
}
