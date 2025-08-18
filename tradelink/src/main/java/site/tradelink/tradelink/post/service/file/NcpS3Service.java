package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NcpS3Service {

    private final S3Client s3Client;

    @Value("${cloud.ncp.s3.bucket}")
    private String bucket;

    public void deleteFiles(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }

        List<String> sanitizedKeys = s3Keys.stream()
                .filter(Objects::nonNull)
                .filter(key -> !key.isBlank())
                .distinct()
                .toList();

        if (sanitizedKeys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> toDelete = sanitizedKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder()
                        .objects(toDelete)
                        .quiet(false)
                        .build())
                .build();

        List<String> successKeys = new ArrayList<>();
        Map<String, String> failureKeys = new HashMap<>();

        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(deleteReq);

            if (response.hasDeleted()) {
                response.deleted().forEach(delete -> successKeys.add(delete.key()));
            }

            if (response.hasErrors()) {
                response.errors().forEach(error -> failureKeys.put(error.key(), error.code() + ": " + error.message()));
            }
        } catch (S3Exception | SdkClientException e) {
            sanitizedKeys.forEach(key -> failureKeys.put(key, e.getClass().getSimpleName() + ": " + e.getMessage()));
        }

        // 추후 Error 처리 작업 필요
        if (!failureKeys.isEmpty()) {
//            throw new S3CleanupException(successKeys, failureKeys);
            throw new IllegalArgumentException("S3 처리가 실패하였습니다.");
        }
    }
}
