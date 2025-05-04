package org.example.urlshortenerread.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.urlshortenerread.model.UrlDocument;
import org.example.urlshortenerread.model.UrlEvent;
import org.example.urlshortenerread.repository.UrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UrlEventConsumer.class);

    private final UrlRepository urlRepository;
    private final RedisTemplate<String, UrlDocument> redisTemplate;

    private static final String URL_CACHE_PREFIX = "url:";
    private static final long CACHE_TTL = 24 * 60 * 60;

    @KafkaListener(topics = "${spring.kafka.topic.url-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, UrlEvent> record) {
        logger.info("Received URL event: {}", record.key());
        UrlEvent event = record.value();

        UrlDocument document = UrlDocument.builder()
                .shortUrl(event.getShortUrl())
                .originalUrl(event.getOriginalUrl())
                .createdAt(event.getCreatedAt())
                .shortUrlIndex(event.getShortUrl())
                .build();

        urlRepository.save(document);

        // Cache the document
        String cacheKey = URL_CACHE_PREFIX + "short:" + event.getShortUrl();
        redisTemplate.opsForValue().set(cacheKey, document, CACHE_TTL, TimeUnit.SECONDS);

        logger.info("Saved URL event to database: {}", event.getShortUrl());
    }
}