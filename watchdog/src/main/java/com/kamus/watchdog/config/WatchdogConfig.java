package com.kamus.watchdog.config;

import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:secrets.properties")
@EntityScan(basePackages = { "com.kamus.core.db", "com.kamus.watchdog.db"})
public class WatchdogConfig {

    private static final String GITHUB_OAUTH_PROPERTY = "oauth";

    @Value("${github.api.bearer}")
    private String githubToken;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public GitHub github() throws IOException {
        Properties properties = new Properties();
        properties.put(GITHUB_OAUTH_PROPERTY, githubToken);

        return GitHubBuilder.fromProperties(properties).build();
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(@NotNull CorsRegistry registry) {
//                registry.addMapping("/api").allowedOrigins("*");
//            }
//        };
//    }

}
