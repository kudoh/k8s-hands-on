package com.example.githubservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Component
public class GithubHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final GithubProps props;
    private WebClient client;

    public GithubHandler(GithubProps props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        this.client = WebClient.builder()
                .baseUrl(props.getApiUrl())
                .build();
    }

    Mono<ServerResponse> query(ServerRequest request) {
        log.info("@@@@{}", props);
        UriComponents uri = UriComponentsBuilder.fromPath(props.getRepoSearchPath())
                .queryParam("q", request.queryParam("query").orElse("")).build();

        return client.get()
                .uri(uri.toUriString())
                .accept(MediaType.valueOf(props.getMediaType()))
                .headers(headers -> headers.setBasicAuth(props.getUser(), props.getPassword()))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {})
                .map(resp -> resp.get("items"))
                .flux()
                .map(Repository::fromGithub)
                .reduce(new ArrayList<>(), (acc, repository) -> {
                    acc.add(repository);
                    return acc;
                })
                .flatMap(items -> ServerResponse.ok().body(BodyInserters.fromObject(items)));
    }
}
