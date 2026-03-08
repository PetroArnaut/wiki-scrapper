package com.lastminute.recruitment.domain;

import com.lastminute.recruitment.domain.exception.WikiPageNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WikiScrapperTest {

    @Test
    void shouldScrapTheWholeGraphAndAvoidLoops() {
        Map<String, WikiPage> pages = new LinkedHashMap<>();
        pages.put("http://wikiscrapper.test/site1", new WikiPage("Site 1", "Content1", "http://wikiscrapper.test/site1", List.of("http://wikiscrapper.test/site2", "http://wikiscrapper.test/site3")));
        pages.put("http://wikiscrapper.test/site2", new WikiPage("Site 2", "Content2", "http://wikiscrapper.test/site2", List.of("http://wikiscrapper.test/site4", "http://wikiscrapper.test/site5")));
        pages.put("http://wikiscrapper.test/site3", new WikiPage("Site 3", "Content3", "http://wikiscrapper.test/site3", List.of("http://wikiscrapper.test/site1", "http://wikiscrapper.test/site5")));
        pages.put("http://wikiscrapper.test/site4", new WikiPage("Site 4", "Content4", "http://wikiscrapper.test/site4", List.of("http://wikiscrapper.test/site1", "http://wikiscrapper.test/site2")));
        pages.put("http://wikiscrapper.test/site5", new WikiPage("Site 5", "Content5", "http://wikiscrapper.test/site5", List.of("http://wikiscrapper.test/site1", "http://wikiscrapper.test/site2", "http://wikiscrapper.test/site3", "http://wikiscrapper.test/site4")));

        AtomicInteger readerCalls = new AtomicInteger();
        WikiPageReader reader = link -> {
            readerCalls.incrementAndGet();
            WikiPage page = pages.get(link);
            if (page == null) {
                throw new WikiPageNotFound("Missing page: " + link);
            }
            return page;
        };

        List<WikiPage> saved = new ArrayList<>();
        WikiScrapper scrapper = new WikiScrapper(reader, saved::add);
        scrapper.read("http://wikiscrapper.test/site1");

        assertThat(saved)
                .extracting(WikiPage::selfLink)
                .containsExactlyInAnyOrderElementsOf(pages.keySet());
        assertThat(saved)
                .hasSize(5);
        assertThat(readerCalls.get())
                .isEqualTo(5);
    }

    @Test
    void shouldHandleTrailingSlashInRootLink() {
        Map<String, WikiPage> pages = Map.of(
                "http://wikiscrapper.test/site2", new WikiPage("Site 2", "Content2", "http://wikiscrapper.test/site2", List.of())
        );

        AtomicInteger readerCalls = new AtomicInteger();
        WikiPageReader reader = link -> {
            readerCalls.incrementAndGet();
            WikiPage page = pages.get(link);
            if (page != null) {
                return page;
            }
            throw new WikiPageNotFound("Missing page: " + link);
        };

        List<WikiPage> saved = new ArrayList<>();
        WikiScrapper scrapper = new WikiScrapper(reader, saved::add);
        scrapper.read("  http://wikiscrapper.test/site2/ ");

        assertThat(saved)
                .extracting(WikiPage::selfLink)
                .containsExactly("http://wikiscrapper.test/site2");
        assertThat(readerCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldAvoidDuplicatePagesInCycles() {
        Map<String, WikiPage> pages = Map.of(
                "http://wikiscrapper.test/site2", new WikiPage("Site 2", "Content2", "http://wikiscrapper.test/site2", List.of(
                        "http://wikiscrapper.test/site4",
                        "http://wikiscrapper.test/site5"
                )),
                "http://wikiscrapper.test/site4", new WikiPage("Site 4", "Content4", "http://wikiscrapper.test/site4", List.of("http://wikiscrapper.test/site2")),
                "http://wikiscrapper.test/site5", new WikiPage("Site 5", "Content5", "http://wikiscrapper.test/site5", List.of("http://wikiscrapper.test/site2"))
        );

        AtomicInteger readerCalls = new AtomicInteger();
        WikiPageReader reader = link -> {
            readerCalls.incrementAndGet();
            WikiPage page = pages.get(link);
            if (page == null) {
                throw new WikiPageNotFound("Missing page: " + link);
            }
            return page;
        };

        List<WikiPage> saved = new ArrayList<>();
        WikiScrapper scrapper = new WikiScrapper(reader, saved::add);
        scrapper.read("http://wikiscrapper.test/site2");

        assertThat(saved)
                .extracting(WikiPage::selfLink)
                .containsExactlyInAnyOrder(
                        "http://wikiscrapper.test/site2",
                        "http://wikiscrapper.test/site4",
                        "http://wikiscrapper.test/site5"
                );
        assertThat(readerCalls.get()).isEqualTo(3);
    }

    @Test
    void shouldHandleSinglePageWithoutOutgoingLinks() {
        AtomicInteger readerCalls = new AtomicInteger();
        WikiPageReader reader = link -> {
            readerCalls.incrementAndGet();
            return new WikiPage("Site 1", "Content1", link, List.of());
        };

        List<WikiPage> saved = new ArrayList<>();
        WikiScrapper scrapper = new WikiScrapper(reader, saved::add);
        scrapper.read("http://wikiscrapper.test/site1");

        assertThat(saved).extracting(WikiPage::selfLink).containsExactly("http://wikiscrapper.test/site1");
        assertThat(readerCalls.get()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenRootPageDoesNotExist() {
        WikiPageReader reader = link -> {
            throw new WikiPageNotFound("Missing page: " + link);
        };

        WikiScrapper scrapper = new WikiScrapper(reader, page -> {
        });

        assertThatThrownBy(() -> scrapper.read("http://wikiscrapper.test/no-such-page"))
                .isInstanceOf(WikiPageNotFound.class);
    }

    @Test
    void shouldThrowWhenReaderReturnsNull() {
        WikiPageReader reader = link -> null;

        WikiScrapper scrapper = new WikiScrapper(reader, page -> {
        });

        assertThatThrownBy(() -> scrapper.read("http://wikiscrapper.test/site1"))
                .isInstanceOf(WikiPageNotFound.class)
                .hasMessage("Reader returned no page for: http://wikiscrapper.test/site1");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldThrowWhenRootLinkIsNullOrBlank(String link) {
        WikiScrapper scrapper = new WikiScrapper(ignoredLink -> null, page -> {
        });

        assertThatThrownBy(() -> scrapper.read(link))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Root link must not be empty");
    }
}
