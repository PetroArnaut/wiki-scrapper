package com.lastminute.recruitment.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lastminute.recruitment.domain.WikiPage;
import com.lastminute.recruitment.domain.WikiPageReader;
import com.lastminute.recruitment.client.dto.WikiPageDto;
import com.lastminute.recruitment.domain.exception.WikiPageNotFound;
import com.lastminute.recruitment.domain.exception.WikiPageParseException;

import java.io.IOException;
import java.io.InputStream;

public class JsonWikiClient implements WikiPageReader {
    private static final String RESOURCE_PREFIX = "wikiscrapper/";
    private static final String RESOURCE_EXTENSION = ".json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public WikiPage read(String link) {
        String pageId = ParseUtils.parsePageId(link);
        String resource = RESOURCE_PREFIX + pageId + RESOURCE_EXTENSION;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new WikiPageNotFound("Page not found for link: " + link);
            }
            WikiPageDto dto = OBJECT_MAPPER.readValue(inputStream, WikiPageDto.class);
            return new WikiPage(dto.title(), dto.content(), dto.selfLink(), dto.links());
        } catch (IOException e) {
            throw new WikiPageParseException("Invalid JSON for: " + link, e);
        }
    }
}
