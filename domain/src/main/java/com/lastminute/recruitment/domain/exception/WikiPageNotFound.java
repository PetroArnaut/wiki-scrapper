package com.lastminute.recruitment.domain.exception;

public class WikiPageNotFound extends RuntimeException{
    public WikiPageNotFound(String message) {
        super(message);
    }
}
