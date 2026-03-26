package site.tradelink.tradelink.cryptocurrency.inMemory;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 변경된 종목 추적기
 *
 * 빗썸 데이터 수신 / 체결 완료 시 ticker를 dirty로 표시
 * SnapshotScheduler가 1초마다 dirty 목로을 가져가서 SSE broadcast
 */
@Component
public class DirtyTracker {

    private final Set<String> dirty = ConcurrentHashMap.newKeySet();

    public void markDirty(String ticker) {
        dirty.add(ticker);
    }

    public Set<String> getAndClear() {
        Set<String> snapshot = ConcurrentHashMap.newKeySet();
        dirty.forEach(ticker -> {
            if (dirty.remove(ticker)) {
                snapshot.add(ticker);
            }
        });
        return snapshot;
    }
}
