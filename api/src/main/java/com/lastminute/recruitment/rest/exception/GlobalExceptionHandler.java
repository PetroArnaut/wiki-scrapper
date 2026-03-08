package com.lastminute.recruitment.rest.exception;

import com.lastminute.recruitment.domain.exception.WikiPageNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WikiPageNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void wikiPageNotFound(WikiPageNotFound ex) {
        log.warn("Wiki page not found: {}", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void badRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleUnexpected(Exception ex) {
        log.error("Unexpected error during scraping", ex);
    }
}
