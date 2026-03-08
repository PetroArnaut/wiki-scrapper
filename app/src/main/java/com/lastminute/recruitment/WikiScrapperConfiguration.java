package com.lastminute.recruitment;

import com.lastminute.recruitment.client.HtmlWikiClient;
import com.lastminute.recruitment.client.JsonWikiClient;
import com.lastminute.recruitment.domain.WikiPageReader;
import com.lastminute.recruitment.domain.WikiScrapper;
import com.lastminute.recruitment.persistence.WikiPageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class WikiScrapperConfiguration {

    @Bean
    @Profile("json & !html")
    public WikiPageReader jsonWikiReader() {
        return new JsonWikiClient();
    }

    @Bean
    @Profile("html & !json")
    public WikiPageReader htmlWikiReader() {
        return new HtmlWikiClient();
    }

    @Bean
    public WikiPageRepository wikiPageRepository() {
        return new WikiPageRepository();
    }

    @Bean
    public WikiScrapper wikiScrapper(WikiPageReader wikiPageReader, WikiPageRepository wikiPageRepository) {
        return new WikiScrapper(wikiPageReader, wikiPageRepository::save);
    }
}
