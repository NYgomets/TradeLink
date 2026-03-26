package site.tradelink.tradelink.post.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class AllowedImageContentTypeTest {

    @Nested
    @DisplayName("isAllowed")
    class IsAllowed {

        @ParameterizedTest
        @ValueSource(strings = {"image/jpeg", "image/png", "image/webp"})
        @DisplayName("허용된 MIME 타입은 true를 반환한다")
        void returnsTrueWhenAllowedMimeType(String contentType) {
            assertThat(AllowedImageContentType.isAllowed(contentType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"IMAGE/JPEG", "Image/Png", "IMAGE/WEBP"})
        @DisplayName("대소문자 관계없이 허용된 타입이면 true를 반환한다")
        void returnsTrueRegardlessOfCase(String contentType) {
            assertThat(AllowedImageContentType.isAllowed(contentType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"application/pdf", "text/plain", "image/gif", "video/mp4"})
        @DisplayName("허용되지 않은 MIME 타입은 false를 반환한다")
        void returnsFalseWhenNotAllowedMimeType(String contentType) {
            assertThat(AllowedImageContentType.isAllowed(contentType)).isFalse();
        }

        @Test
        @DisplayName("null이면 false를 반환한다")
        void returnsFalseWhenNull() {
            assertThat(AllowedImageContentType.isAllowed(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열이면 false를 반환한다")
        void returnsFalseWhenEmpty() {
            assertThat(AllowedImageContentType.isAllowed("")).isFalse();
        }

        @Test
        @DisplayName("공백 문자열이면 false를 반환한다")
        void returnsFalseWhenBlank() {
            assertThat(AllowedImageContentType.isAllowed("   ")).isFalse();
        }
    }
}
