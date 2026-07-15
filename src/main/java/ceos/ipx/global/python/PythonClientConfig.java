package ceos.ipx.global.python;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Python 검색/분석 서버와 통신하기 위한 WebClient 설정
 */
@Configuration
public class PythonClientConfig {

    @Value("${python.server.base-url}")
    private String pythonBaseUrl;

    @Value("${python.server.timeout-seconds:120}")
    private int timeoutSeconds;

    /**
     * Python 서버 통신 전용 WebClient.
     * Bean 이름을 명시적으로 지정해 다른 WebClient와 구분.
     */
    @Bean(name = "pythonWebClient")
    public WebClient pythonWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)   // 연결 10초
                .responseTimeout(Duration.ofSeconds(timeoutSeconds))    // 응답 대기 120초
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeoutSeconds, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(pythonBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))   // 응답 최대 크기: 10MB
                .build();
    }
}
