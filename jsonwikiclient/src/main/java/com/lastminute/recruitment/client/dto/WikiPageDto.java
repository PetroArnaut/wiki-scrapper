package com.lastminute.recruitment.client.dto;

import java.util.List;
import java.util.Objects;

public record WikiPageDto(String title, String content, String selfLink, List<String> links) {
    public WikiPageDto {
        title = Objects.requireNonNullElse(title, "").strip();
        content = Objects.requireNonNullElse(content, "").strip();
        selfLink = Objects.requireNonNull(selfLink, "selfLink must not be null").strip();
        if (selfLink.isBlank()) {
            throw new IllegalArgumentException("selfLink must not be blank");
        }
        links = links == null ? List.of() : List.copyOf(links);
    }
}
