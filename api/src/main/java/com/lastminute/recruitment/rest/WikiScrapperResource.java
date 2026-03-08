package com.lastminute.recruitment.rest;

import com.lastminute.recruitment.domain.WikiScrapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/wiki")
@RestController
public class WikiScrapperResource {
    private final WikiScrapper wikiScrapper;

    public WikiScrapperResource(WikiScrapper wikiScrapper) {
        this.wikiScrapper = wikiScrapper;
    }

    @PostMapping(value = "/scrap", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> scrapWikipedia(@RequestBody String link) {
        wikiScrapper.read(link);
        return ResponseEntity.ok().build();
    }
}
