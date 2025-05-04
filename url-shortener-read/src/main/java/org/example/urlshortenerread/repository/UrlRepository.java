package org.example.urlshortenerread.repository;

import org.example.urlshortenerread.model.UrlDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends MongoRepository<UrlDocument, String> {
    Optional<UrlDocument> findByShortUrl(String shortUrl);
}