package hu.agilexpert.web;

import hu.agilexpert.core.client.CoreApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

    @Bean
    public CoreApiClient coreApiClient(@Value("${core.api.base-url}") String baseUrl) {
        return new CoreApiClient(baseUrl);
    }
}
