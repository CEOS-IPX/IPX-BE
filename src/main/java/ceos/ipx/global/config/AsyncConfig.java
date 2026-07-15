package ceos.ipx.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 실행 설정
 *
 * 검색 요청처럼 Python 서버 호출이 오래 걸리는 작업은
 * @Async과 이 executor를 사용해 별도 스레드에서 처리
 *
 * 사용 예:
 *   @Async("searchTaskExecutor")
 *   public void executeSearchAsync(...) { ... }
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 검색/분석 등 Python 호출용 스레드풀
     */
    @Bean(name = "searchTaskExecutor")
    public Executor searchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("search-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
