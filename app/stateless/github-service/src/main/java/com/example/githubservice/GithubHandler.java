package com.example.githubservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Component
public class GithubHandler {

    private final ObjectMapper objectMapper ;
    private final GithubProps props;
    private final WebClient client;

    Mono<ServerResponse> query(ServerRequest request) {

        UriComponents uri = UriComponentsBuilder.fromPath(props.getRepoSearchPath())
                .queryParam("q", request.queryParam("query").orElse(""))
                .build();

        log.info("retrieving repositories from github for {}", uri.toUriString());
        //noinspection unchecked
        return client.get()
                .uri(uri.toUriString())
                .accept(MediaType.valueOf(props.getMediaType()))
                .headers(headers -> headers.setBasicAuth(props.getUser(), props.getPassword()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                    response -> Mono.error(new RuntimeException("Client Error " + response.statusCode())))
                .onStatus(HttpStatus::is5xxServerError,
                    response -> Mono.error(new RuntimeException("Server Error " + response.statusCode())))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMapIterable(resp -> (List<Object>) resp.get("items"))
                .map(in -> Repository.fromGithub(in, objectMapper))
                .collectList()
                .flatMap(items -> ServerResponse.ok().body(BodyInserters.fromObject(items)));
    }
}
