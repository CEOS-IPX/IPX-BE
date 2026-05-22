package ceos.ipx.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ipxOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("IPX API Documentation")
                .description("OpenAPI documentation for the IPX Spring Boot application.")
                .version("v1"));
    }
}
