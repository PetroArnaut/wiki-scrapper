package com.lastminute.recruitment;

import com.lastminute.recruitment.domain.WikiPage;
import com.lastminute.recruitment.persistence.WikiPageRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("json")
@Import(WikiScrapperResourceTest.TestWikiPageRepositoryConfig.class)
class WikiScrapperResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryWikiPageRepository wikiPageRepository;

    @BeforeEach
    void setUp() {
        wikiPageRepository.clear();
    }

    @Test
    void shouldReturn200AndPersistJsonWikiPages() throws Exception {
        mockMvc.perform(post("/wiki/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"http://wikiscrapper.test/site2\""))
                .andExpect(status().isOk());

        List<WikiPage> savedPages = wikiPageRepository.savedPages();
        assertThat(savedPages).hasSize(5);
        assertThat(savedPages)
                .extracting(WikiPage::selfLink)
                .containsExactlyInAnyOrder(
                        "http://wikiscrapper.test/site1",
                        "http://wikiscrapper.test/site2",
                        "http://wikiscrapper.test/site3",
                        "http://wikiscrapper.test/site4",
                        "http://wikiscrapper.test/site5"
                );
    }

    @Test
    void shouldReturn404WhenRootJsonPageDoesNotExist() throws Exception {
        mockMvc.perform(post("/wiki/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"http://wikiscrapper.test/no-such-page\""))
                .andExpect(status().isNotFound());

        assertThat(wikiPageRepository.savedPages()).isEmpty();
    }

    @TestConfiguration
    static class TestWikiPageRepositoryConfig {

        @Bean
        @Primary
        InMemoryWikiPageRepository testWikiPageRepository() {
            return new InMemoryWikiPageRepository();
        }
    }
}

class InMemoryWikiPageRepository extends WikiPageRepository {
    private final List<WikiPage> pages = new ArrayList<>();

    @Override
    public void save(WikiPage wikiPage) {
        pages.add(wikiPage);
    }

    List<WikiPage> savedPages() {
        return List.copyOf(pages);
    }

    void clear() {
        pages.clear();
    }
}
