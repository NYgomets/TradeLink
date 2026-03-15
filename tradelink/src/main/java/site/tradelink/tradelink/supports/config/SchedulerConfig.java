package site.tradelink.tradelink.supports.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import site.tradelink.tradelink.like.common.scheduler.ProcessLikeEvents;
import site.tradelink.tradelink.like.common.scheduler.failed.DLQRetryScheduler;

import java.time.Duration;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig implements SchedulingConfigurer {

    private final ProcessLikeEvents processLikeEvents;
    private final DLQRetryScheduler dlqRetryScheduler;

    // 전역 공용 스케줄러 - @Scheduled 어노테이션이 달린 다른 태스크들이 사용
    @Bean(name = "globalScheduler")
    public ThreadPoolTaskScheduler globalScheduler() {
        return buildScheduler("scheduler-global-", 5);
    }

    // ProcessLikeEvents 전용 스케줄러 - 스레드 1개
    @Bean(name = "likeEventScheduler")
    public ThreadPoolTaskScheduler likeEventScheduler() {
        return buildScheduler("scheduler-like-", 1);
    }

    // DLQRetryScheduler 전용 스케줄러 - 스레드 1개
    @Bean(name = "dlqScheduler")
    public ThreadPoolTaskScheduler dlqScheduler() {
        return buildScheduler("scheduler-dlq-", 1);
    }

    // 전역 스케줄러를 @Scheduled 기본 스케줄러로 등록
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(globalScheduler());
    }

    // like/dlq 스케줄러는 앱 시작 시 직접 등록
    @Bean
    public ApplicationRunner registerLikeSchedules(
            @Qualifier("likeEventScheduler") ThreadPoolTaskScheduler likeScheduler,
            @Qualifier("dlqScheduler") ThreadPoolTaskScheduler dlqScheduler
    ) {
        return args -> {
            likeScheduler.scheduleWithFixedDelay(
                    processLikeEvents::processLikeEvents,
                    Duration.ofMillis(1000)
            );
            dlqScheduler.scheduleWithFixedDelay(
                    dlqRetryScheduler::retryFailedEvents,
                    Duration.ofMillis(60000)
            );
        };


    }

    private ThreadPoolTaskScheduler buildScheduler(String prefix, int poolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(prefix);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

}
