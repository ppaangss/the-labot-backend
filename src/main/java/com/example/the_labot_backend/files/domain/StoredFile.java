package com.example.the_labot_backend.files.domain;

public record StoredFile(
        String originalFileName,
        String storedFileName,
        String fileUrl,
        String contentType,
        long size
){}
