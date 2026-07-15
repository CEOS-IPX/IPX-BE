package ceos.ipx.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "frontend.oauth")
public record FrontendOAuthProperties(
        String googleCallbackUri,
        List<String> allowedRedirectUris
) {
}