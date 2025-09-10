package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    public Map<String, String> deleteFiles(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> sanitizedKeys = s3Keys.stream()
                .filter(Objects::nonNull)
                .filter(key -> !key.isBlank())
                .distinct()
                .toList();

        List<ObjectIdentifier> toDelete = sanitizedKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        if (toDelete.isEmpty()) {
            return Collections.emptyMap();
        }

        DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder()
                        .objects(toDelete)
                        .quiet(false)
                        .build())
                .build();

        Map<String, String> failureKeys = new HashMap<>();

        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(deleteReq);

            if (response.hasErrors()) {
                response.errors().forEach(error -> failureKeys.put(error.key(), error.code() + ": " + error.message()));
            }
        } catch (S3Exception e) {
            sanitizedKeys.forEach(key -> failureKeys.put(key, e.awsErrorDetails().errorMessage()));
        }

        return failureKeys;
    }

    public List<S3Object> listAllObjectsByPrefix(String prefix) {
        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            return s3Client.listObjectsV2(listReq).contents();
        } catch (S3Exception e) {
            return Collections.emptyList();
        }
    }
}
