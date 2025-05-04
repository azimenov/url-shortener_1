package org.example.urlshortenerwrite.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlEvent implements Serializable {
    private Integer id;
    private String originalUrl;
    private String shortUrl;
    private LocalDateTime createdAt;
}