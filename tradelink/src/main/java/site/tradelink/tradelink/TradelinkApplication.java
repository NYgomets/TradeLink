package site.tradelink.tradelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TradelinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradelinkApplication.class, args);
	}

}
