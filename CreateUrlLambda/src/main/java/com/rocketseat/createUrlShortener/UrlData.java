package com.rocketseat.createUrlShortener;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UrlData {
    private String originalUrl;
    private long expirationTime;

    public UrlData(String originalUrl, long expirationTimeInSeconds) {
        this.originalUrl = originalUrl;
        this.expirationTime = expirationTimeInSeconds;
    }
}
