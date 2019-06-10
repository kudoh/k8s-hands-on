package com.example.githubservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class Repository {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private Long id;
    private String name;
    private String fullName;
    private String owner;
    private String avatarUrl;
    private String htmlUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int size;
    private int starGazersCount;
    private int watchersCount;
    private String language;
    private int forkCount;
    private double score;

    public static Repository fromGithub(Map<String, Object> input) {
        return objectMapper.convertValue(input, Repository.class);
//        Map<String, Object> owner = (Map<String, Object>) input.get("owner");
//        return new Repository(
//                (Long)input.get("id"), (String)input.get("name"), input.get("full_name"),
//                owner.get("login"), owner.get("avatar_url"), input.get("html_url"),
//                input.get("description"), LocalDateTime.parse((String)input.get("created_at")), LocalDateTime.parse((String)input.get("updated_at")),
//                (int)input.get("size"), (int)input.get("stargazers_count"), (int)input.get("watchers_count"),
//                input.get("language"), (int)input.get("fork_count"), (double)input.get("score")
//        );
    }
}
