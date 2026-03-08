package com.lastminute.recruitment.domain;

public interface WikiPageReader {
    /**
     * Reads a wiki page from the provided link.
     *
     * @param link non-null, normalized wiki link
     * @return a non-null WikiPage
     * @throws com.lastminute.recruitment.domain.exception.WikiPageNotFound when the page cannot be resolved
     */
    WikiPage read(String link);
}
