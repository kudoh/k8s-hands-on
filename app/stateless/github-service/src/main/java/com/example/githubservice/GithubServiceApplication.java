package com.example.githubservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class GithubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubServiceApplication.class, args);
    }

    @Bean
    WebClient webClient(GithubProps props) {
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }
}
