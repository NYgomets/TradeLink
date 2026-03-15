package site.tradelink.tradelink.stock.authorization.scheduler.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class BokRestClientConfig {

    @Value("${bok.api.base-url}")
    private String bokBaseUrl;

    @Value("${bok.api.timeout:10000}")
    private int timeout;

    /**
     * 한구은행 API용 RestClient
     * JSON 응답 사용
     */
    @Bean
    public RestClient bokRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        return RestClient.builder()
                .baseUrl(bokBaseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .requestInterceptor((request, body, execution) -> {
                    log.debug("[BOK API] {} {}", request.getMethod(), request.getURI());
                    long start = System.currentTimeMillis();
                    var response = execution.execute(request, body);
                    log.debug("[BOK API] {} ({}ms)", response.getStatusCode(), System.currentTimeMillis() - start);
                    return response;
                })
                .build();
    }
}
