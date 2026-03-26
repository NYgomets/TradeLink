package site.tradelink.tradelink.post.common.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AllowedImageContentType {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    private final String mimeType;

    // O(1) 조회를 위해 MIME 타입을 소문자로 변환하여 Set에 미리 저장
    private static final Set<String> ALLOWED_MIME_TYPES_LOWERCASE =
            Arrays.stream(values())
                    .map(type -> type.getMimeType().toLowerCase())
                    .collect(Collectors.toSet());

    AllowedImageContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    /**
     * 주어진 contentType String이 허용된 MIME 타입인지 확인
     * 시간복잡도 : O(1)
     * @param contentType 확인할 MIME 타입 문자열
     * @return 허용된 타입이면 true, 아니면 false
     */
    public static boolean isAllowed(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }

        return ALLOWED_MIME_TYPES_LOWERCASE.contains(contentType.toLowerCase());
    }
}
