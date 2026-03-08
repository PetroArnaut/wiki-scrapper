package com.lastminute.recruitment.client;

import com.lastminute.recruitment.domain.exception.WikiPageNotFound;

public final class ParseUtils {
    private ParseUtils() {
    }

    public static String parsePageId(String link) {
        if (link == null || link.isBlank()) {
            throw new WikiPageNotFound("Null link provided");
        }
        String normalized = link.strip().replaceAll("/+$", "");
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex < 0) {
            throw new WikiPageNotFound("Invalid wiki link: " + link);
        }
        return normalized.substring(slashIndex + 1);
    }
}
