package com.lastminute.recruitment.domain;

import java.util.List;
import java.util.Objects;

public record WikiPage(String title, String content, String selfLink, List<String> links) {
    public WikiPage {
        title = Objects.requireNonNull(title, "title must not be null").strip();
        content = Objects.requireNonNull(content, "content must not be null").strip();
        selfLink = Objects.requireNonNull(selfLink, "selfLink must not be null").strip();
        if (selfLink.isBlank()) {
            throw new IllegalArgumentException("selfLink must not be blank");
        }
        links = links == null ? List.of() : List.copyOf(links);
    }
}
