package site.tradelink.tradelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 스케줄링 활성화
@EnableAsync // 비동기 처리 활성화
@EnableRetry // 재시도 기능 활성화
@SpringBootApplication
public class TradelinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradelinkApplication.class, args);
	}

}
