package com.strauteka.example.controller;

import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Objects;

public class ControllerUtils {

    /**
     * @param acceptContentType requested mediaType
     * @param defaultIfAcceptContentTypeNull default MediaType
     * @param mediaTypes mediaTypes supported
     * @return if @acceptContentType is null: @defaultIfAcceptContentTypeNull, else requested mediaType
     * @throws RuntimeException if @acceptContentType requested content is not supported
     */
    public static MediaType mediaTypeCheck(String acceptContentType,
                                     MediaType defaultIfAcceptContentTypeNull,
                                     String... mediaTypes) {
        if (Objects.nonNull(acceptContentType)) {
            String contentType = findContentType(acceptContentType, mediaTypes);
            if (Objects.isNull(contentType)) {
                throw new RuntimeException(
                        String.format("Can't produce required content: %s Allowed: %s",
                                acceptContentType,
                                Arrays.toString(mediaTypes))
                );
            }
            final MediaType mediaType = MediaType.valueOf(contentType);
            return mediaType.equals(MediaType.ALL) ? defaultIfAcceptContentTypeNull : mediaType;
        }
        return defaultIfAcceptContentTypeNull;
    }

    private static String findContentType(String acceptContentType, String... mediaTypes) {
        for (String n : mediaTypes) {
            if (acceptContentType.equalsIgnoreCase(n)) {
                return n;
            }
        }
        return null;
    }
}
