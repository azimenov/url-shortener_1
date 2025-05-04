package org.example.urlshortenerwrite.controller;

import lombok.RequiredArgsConstructor;
import org.example.urlshortenerwrite.model.Url;
import org.example.urlshortenerwrite.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<Url> createShortUrl(@RequestParam(value = "originalUrl") String originalUrl) {
        Url url = this.urlService.createUrl(originalUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(url);
    }
}