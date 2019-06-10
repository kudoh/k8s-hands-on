package com.example.githubservice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("github")
public class GithubProps {
    private String apiUrl;
    private String mediaType;
    private String user;
    private String password;
    private String repoSearchPath;
}
