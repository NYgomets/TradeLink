package site.tradelink.tradelink.stock.authorization.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.stock.authorization.manager.KiwoomAccessTokenManager;

@Component
@RequiredArgsConstructor
public class TokenScheduler {

    private final KiwoomAccessTokenManager kiwoomAccessTokenManager;

    @Scheduled(initialDelay = 0, fixedDelay = 6 * 60 * 60 * 1000)
    public void checkToken() {
        kiwoomAccessTokenManager.getAccessToken();
    }
}
