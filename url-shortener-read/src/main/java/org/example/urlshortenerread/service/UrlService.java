package org.example.urlshortenerread.service;

import lombok.RequiredArgsConstructor;
import org.example.urlshortenerread.model.UrlDocument;
import org.example.urlshortenerread.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, UrlDocument> redisTemplate;

    private static final String URL_CACHE_PREFIX = "url:";
    private static final long CACHE_TTL = 24 * 60 * 60;

    public UrlDocument getOriginalUrlByShortUrl(String shortUrl) {
        logger.info("Retrieving original URL for short URL: {}", shortUrl);
        String cacheKey = URL_CACHE_PREFIX + "short:" + shortUrl;

        // Skip Redis initially due to serialization issues
        UrlDocument url = null;

        try {
            url = this.urlRepository.findByShortUrl(shortUrl)
                    .orElseThrow(() -> {
                        logger.error("URL not found for short URL: {}", shortUrl);
                        return new RuntimeException(String.format("'%s' not found", shortUrl));
                    });

            // Try to update Redis with the correct format
            try {
                logger.debug("Caching URL with key: {}", cacheKey);
                redisTemplate.opsForValue().set(cacheKey, url, CACHE_TTL, TimeUnit.SECONDS);
                logger.info("Successfully cached URL for short URL: {}", shortUrl);
            } catch (Exception e) {
                logger.warn("Failed to cache URL: {}", e.getMessage());
                // Continue execution even if caching fails
            }

            return url;
        } catch (Exception e) {
            logger.error("Error retrieving URL for short URL: {}", shortUrl, e);
            throw e;
        }
    }
}