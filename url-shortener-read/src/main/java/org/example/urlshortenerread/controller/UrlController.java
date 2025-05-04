package org.example.urlshortenerread.controller;

import lombok.RequiredArgsConstructor;
import org.example.urlshortenerread.model.UrlDocument;
import org.example.urlshortenerread.service.UrlService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @GetMapping("/shortUrl/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortUrl) throws URISyntaxException {
        UrlDocument urlByShortUrl = this.urlService.getOriginalUrlByShortUrl(shortUrl);
        String redirectTo = urlByShortUrl.getOriginalUrl();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(new URI(redirectTo));
        return new ResponseEntity<>(httpHeaders, HttpStatus.MOVED_PERMANENTLY);
    }
}