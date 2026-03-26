package site.tradelink.tradelink.cryptocurrency.sse.dto;

public record SseEvent(String eventName, Object data) {

    public static SseEvent poison() {
        return new SseEvent("__CLOSE__", null);
    }

    public boolean isPoison() {
        return "__CLOSE__".equals(eventName);
    }
}
