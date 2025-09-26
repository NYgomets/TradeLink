package site.tradelink.tradelink.post.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.post.common.exception.S3DeletionException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

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

        List<ObjectIdentifier> toDelete = sanitizedKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .collect(Collectors.toList());

        if (toDelete.isEmpty()) {
            return;
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

        if (!failureKeys.isEmpty()) {
            throw new S3DeletionException("S3 파일 삭제 중 일부 또는 전체 실패", failureKeys);
        }
    }

    /**
     * S3 객체 목록을 페이지네이션하여 가져오는 Iterable을 반환
     * 대용량 데이터를 메모리 문제 없이 처리하기 위해 사용
     */
    public ListObjectsV2Iterable listObjectsByPrefixPaginated(String prefix) {
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        return s3Client.listObjectsV2Paginator(listReq);
    }
}
