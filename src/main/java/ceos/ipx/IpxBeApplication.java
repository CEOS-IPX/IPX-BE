package ceos.ipx;

import ceos.ipx.global.config.FrontendOAuthProperties;
import ceos.ipx.global.config.GoogleOAuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({
		GoogleOAuthProperties.class,
		FrontendOAuthProperties.class
})
@SpringBootApplication
public class IpxBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(IpxBeApplication.class, args);
	}
}