package site.tradelink.tradelink.post.common.event;

import lombok.Getter;

import java.util.Map;

@Getter
public class S3DeletionFailureEvent{

    private final Map<String, String> failedKeysWithReason;

    public S3DeletionFailureEvent(Map<String, String> failedKeysWithReason) {
        this.failedKeysWithReason = failedKeysWithReason;
    }
}
