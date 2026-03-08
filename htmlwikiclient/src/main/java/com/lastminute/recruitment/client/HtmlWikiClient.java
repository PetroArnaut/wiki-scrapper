package com.lastminute.recruitment.client;

import com.lastminute.recruitment.domain.WikiPage;
import com.lastminute.recruitment.domain.WikiPageReader;
import com.lastminute.recruitment.domain.exception.WikiPageNotFound;
import com.lastminute.recruitment.domain.exception.WikiPageParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HtmlWikiClient implements WikiPageReader {
    private static final String RESOURCE_PREFIX = "wikiscrapper/";
    private static final String RESOURCE_EXTENSION = ".html";
    private static final String LINKS_SELECTOR = "ul.links a[href]";

    @Override
    public WikiPage read(String link) {
        String pageId = ParseUtils.parsePageId(link);
        Document document = parsePage(pageId, link);

        return new WikiPage(
                require(document.selectFirst("h1.title"), "title").text().trim(),
                require(document.selectFirst("p.content"), "content").text().trim(),
                extractSelfLink(document.selectFirst("meta[selfLink]")),
                extractLinks(document)
        );
    }

    private Document parsePage(String pageId, String link) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                RESOURCE_PREFIX + pageId + RESOURCE_EXTENSION)) {
            if (inputStream == null) {
                throw new WikiPageNotFound("Page not found for link: " + link);
            }
            return Jsoup.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new WikiPageParseException("Failed to read wiki page: " + link, e);
        }
    }

    private String extractSelfLink(Element element) {
        String value = require(element, "selfLink").attr("selfLink").trim();
        if (value.isBlank()) {
            throw new WikiPageParseException("Missing field value: selfLink");
        }
        return value;
    }

    private List<String> extractLinks(Document page) {
        return page.select(LINKS_SELECTOR).stream()
                .map(a -> a.attr("href"))
                .filter(href -> !href.isBlank())
                .toList();
    }

    private Element require(Element element, String field) {
        if (element == null) {
            throw new WikiPageParseException("Missing field: " + field);
        }
        return element;
    }
}
