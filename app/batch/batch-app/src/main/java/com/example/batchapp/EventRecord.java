package com.example.batchapp;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class EventRecord {

    private long id;

    @NonNull
    private EventType type;

    @NonNull
    private LocalDateTime createdAt;

    @NonNull
    private String repoName;

    @NonNull
    private String repoUrl;

    @NonNull
    private String author;

    private String organization;
}
