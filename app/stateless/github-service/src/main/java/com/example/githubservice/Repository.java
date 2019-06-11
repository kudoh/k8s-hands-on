package com.example.githubservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
class Repository {

    private Long id;
    private String name;
    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty(access = Access.READ_ONLY)
    private String owner;
    @JsonProperty(value = "avatar_url", access = Access.READ_ONLY)
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;
    private String description;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    private int size;
    @JsonProperty("open_issues_count")
    private int openIssuesCount;
    @JsonProperty("watchers_count")
    private int watchersCount;
    private String language;
    @JsonProperty("fork_count")
    private int forkCount;
    private double score;

    @JsonProperty(access = Access.READ_ONLY)
    private String license;

    @JsonProperty("owner")
    void unpackOwner(Map<String, String> owner) {
        this.owner = owner.get("login");
        this.avatarUrl = owner.get("avatar_url");
    }

    @JsonProperty("license")
    void unpackLicense(Map<String, String> license) {
        if (license == null) {
            this.license = "Unknown";
        } else {
            this.license = license.get("name");
        }
    }

    static Repository fromGithub(Object input, ObjectMapper mapper) {
        log.debug("input:{}", input);
        return mapper.convertValue(input, Repository.class);
    }
}
