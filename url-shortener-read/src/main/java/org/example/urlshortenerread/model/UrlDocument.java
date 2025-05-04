package org.example.urlshortenerread.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "urls")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlDocument {
    @Id
    private String id;
    private String originalUrl;
    private String shortUrl;
    private LocalDateTime createdAt;

    @Indexed(unique = true)
    private String shortUrlIndex;
}