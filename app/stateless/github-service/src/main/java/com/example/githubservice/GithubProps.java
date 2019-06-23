package com.example.githubservice;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Data
@ConfigurationProperties("github")
public class GithubProps {
    private String host;
    private String protocol;
    private String mediaType;
    private String user;
    private String password;
    private String repoSearchPath;

    String getBaseUrl() {
        return UriComponentsBuilder.newInstance().scheme(protocol).host(host).build().toUriString();
    }    
}
