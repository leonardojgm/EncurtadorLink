package com.rocketseat.redirectUrlShortener;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

//https://nossa-funcao-lambda.com/484jdj3993
public class Main implements RequestHandler<Map<String,Object> , Map<String, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3client = S3Client.builder().build();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameters = (String) input.get("rawPath");
        String shortUrlCode = pathParameters.replace("/", "");

        if (shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-storage-cookie.maia")
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3ObjectStream;

        try {
            s3ObjectStream = s3client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching URL data from S3: " + e.getMessage(), e);
        }

        UrlData urlData;

        try {
            urlData = objectMapper.readValue(s3ObjectStream, UrlData.class);
        } catch (Exception exception) {
            throw new RuntimeException("Error deserializing JSON body: " + exception.getMessage(), exception);
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;
        Map<String, Object> response = new HashMap<>();

        // cenário onde a URL expirou
        if (urlData.getExpirationTime() < currentTimeInSeconds) {
            response.put("statusCode", 410);
            response.put("body", "This URL has expired");

            return response;
        }

        // cenário onde a URL ainda é válida
        Map<String, String> headers = new HashMap<>();

        headers.put("Location", urlData.getOriginalUrl());
        response.put("statusCode", 302);
        response.put("headers", headers);

        return response;
    }
}