package site.tradelink.tradelink.post.common.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class S3DeletionException extends RuntimeException{

    private final Map<String, String> failures;

    public S3DeletionException(String message, Map<String, String> failures) {
        super(message);
        this.failures = failures;
    }
}
