package com.lastminute.recruitment.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/wiki")
@RestController
public class WikiScrapperResource {

    @PostMapping("/scrap")
    public void scrapWikipedia(@RequestBody String link) {
        System.out.println("Hello Scrap -> " + link);
    }

}
