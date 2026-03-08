package com.lastminute.recruitment.domain;

import com.lastminute.recruitment.domain.exception.WikiPageNotFound;

import java.util.HashSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.function.Consumer;

public class WikiScrapper {
    private final WikiPageReader wikiPageReader;
    private final Consumer<WikiPage> wikiPageConsumer;

    public WikiScrapper(WikiPageReader wikiPageReader, Consumer<WikiPage> wikiPageConsumer) {
        this.wikiPageReader = wikiPageReader;
        this.wikiPageConsumer = wikiPageConsumer;
    }

    public void read(String link) {
        String normalizedRootLink = normalize(link);
        if (normalizedRootLink == null || normalizedRootLink.isBlank()) {
            throw new IllegalArgumentException("Root link must not be empty");
        }

        Set<String> visitedLinks = new HashSet<>();
        Set<String> scheduledLinks = new HashSet<>();
        Deque<String> linksToVisit = new ArrayDeque<>();
        linksToVisit.add(normalizedRootLink);
        scheduledLinks.add(normalizedRootLink);

        while (!linksToVisit.isEmpty()) {
            String currentLinkToVisit = linksToVisit.poll();
            if (currentLinkToVisit == null || currentLinkToVisit.isBlank()) {
                continue;
            }

            WikiPage wikiPage = wikiPageReader.read(currentLinkToVisit);
            if (wikiPage == null) {
                throw new WikiPageNotFound("Reader returned no page for: " + currentLinkToVisit);
            }
            String pageSelfLink = normalize(wikiPage.selfLink());
            if (pageSelfLink == null || pageSelfLink.isBlank()) {
                throw new WikiPageNotFound("Invalid page with empty selfLink: " + currentLinkToVisit);
            }

            if (!visitedLinks.add(pageSelfLink)) {
                continue;
            }

            wikiPageConsumer.accept(wikiPage);
            for (String linkToVisit : wikiPage.links()) {
                String normalizedLink = normalize(linkToVisit);
                if (normalizedLink != null && !normalizedLink.isBlank() && scheduledLinks.add(normalizedLink)) {
                    linksToVisit.add(normalizedLink);
                }
            }
        }
    }

    private String normalize(String link) {
        if (link == null) {
            return null;
        }
        return link.strip()
                .replace("\"\"", "\"")
                .replaceAll("^\"|\"$", "")
                .replaceAll("/+$", "");
    }
}
