package org.example.urlshortenerwrite.service;

import lombok.RequiredArgsConstructor;
import org.example.urlshortenerwrite.model.Url;
import org.example.urlshortenerwrite.model.UrlEvent;
import org.example.urlshortenerwrite.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.util.EncodingUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class UrlService {

    private static final Logger logger = LoggerFactory.getLogger(UrlService.class);

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, Url> redisTemplate;
    private final KafkaTemplate<String, UrlEvent> kafkaTemplate;

    private static final String URL_CACHE_PREFIX = "url:";
    private static final long CACHE_TTL = 24 * 60 * 60;

    public Url createUrl(String originalUrl) {
        logger.info("Creating short URL for: {}", originalUrl);
        String cacheKey = URL_CACHE_PREFIX + "original:" + originalUrl;

        // Try to get from cache with error handling
        Url url = null;
        try {
            url = redisTemplate.opsForValue().get(cacheKey);
            if (url != null) {
                logger.info("Cache hit for original URL: {}", originalUrl);
                return url;
            }
        } catch (Exception e) {
            logger.warn("Error retrieving from Redis cache: {}", e.getMessage());
            // Continue execution, will check database instead
        }

        return urlRepository.findByOriginalUrl(originalUrl)
                .map(foundUrl -> {
                    logger.info("URL found in database for original URL: {}", originalUrl);
                    try {
                        cacheUrl(cacheKey, foundUrl);
                    } catch (Exception e) {
                        logger.warn("Failed to cache URL: {}", e.getMessage());
                        // Continue execution even if caching fails
                    }
                    return foundUrl;
                })
                .orElseGet(() -> {
                    logger.info("URL not found in database, generating new short URL for: {}", originalUrl);
                    String shortUrl = generateUniqueShortUrl(originalUrl);

                    Url newUrl = Url.builder()
                            .originalUrl(originalUrl)
                            .shortUrl(shortUrl)
                            .createdAt(LocalDateTime.now())
                            .build();

                    Url savedUrl = urlRepository.save(newUrl);

                    try {
                        cacheUrl(cacheKey, savedUrl);
                    } catch (Exception e) {
                        logger.warn("Failed to cache newly created URL: {}", e.getMessage());
                        // Continue execution even if caching fails
                    }

                    try {
                        sendToKafka(savedUrl);
                    } catch (Exception e) {
                        logger.error("Failed to send URL to Kafka: {}", e.getMessage());
                        // Continue execution even if Kafka send fails
                    }

                    return savedUrl;
                });
    }

    private void cacheUrl(String cacheKey, Url url) {
        logger.debug("Caching URL with key: {}", cacheKey);
        redisTemplate.opsForValue().set(cacheKey, url, CACHE_TTL, TimeUnit.SECONDS);

        // Also cache by short URL for faster lookups
        try {
            String shortUrlCacheKey = URL_CACHE_PREFIX + "short:" + url.getShortUrl();
            redisTemplate.opsForValue().set(shortUrlCacheKey, url, CACHE_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failed to cache URL by short URL: {}", e.getMessage());
            // Continue execution even if this secondary caching fails
        }
    }

    private void sendToKafka(Url url) {
        logger.info("Sending URL to Kafka: {}", url.getShortUrl());
        UrlEvent event = new UrlEvent(url.getId(), url.getOriginalUrl(), url.getShortUrl(), url.getCreatedAt());
        kafkaTemplate.send("url-events", url.getShortUrl(), event);
    }

    private String generateUniqueShortUrl(String originalUrl) {
        logger.debug("Generating unique short URL for: {}", originalUrl);
        return generateUniqueShortUrl(originalUrl, 0);
    }

    private String generateUniqueShortUrl(String originalUrl, int attempts) {
        logger.debug("Attempt {} to generate unique short URL for: {}", attempts, originalUrl);
        final int MAX_ATTEMPTS = 5;
        if (attempts > MAX_ATTEMPTS) {
            logger.warn("Exceeded maximum attempts ({}), adding timestamp to ensure uniqueness", MAX_ATTEMPTS);
            return generateShortUrl(originalUrl + System.currentTimeMillis());
        }

        String shortUrl = generateShortUrl(originalUrl);
        logger.debug("Generated candidate short URL: {}", shortUrl);

        if (urlRepository.existsByShortUrl(shortUrl)) {
            logger.debug("Short URL already exists: {}, trying with suffix", shortUrl);
            String suffix = "_" + attempts;
            return generateUniqueShortUrl(originalUrl + suffix, attempts + 1);
        }

        logger.debug("Unique short URL generated: {}", shortUrl);
        return shortUrl;
    }

    private String generateShortUrl(String originalUrl) {
        logger.trace("Generating hash for URL: {}", originalUrl);
        byte[] bytes = originalUrl.getBytes(StandardCharsets.UTF_8);
        byte[] digest = EncodingUtils.concatenate(KeyGenerators.secureRandom(4).generateKey(), bytes);
        String shortUrl = new String(Hex.encode(digest)).substring(0, 7);
        logger.trace("Generated hash: {}", shortUrl);
        return shortUrl;
    }
}