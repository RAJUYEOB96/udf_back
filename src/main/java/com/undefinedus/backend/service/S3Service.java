package com.undefinedus.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(String key, MultipartFile file);

    void deleteFile(String key);

    String getFileUrl(String key);

    String generateFileKey(String originalFilename);

    String extractKeyFromUrl(String s3Url);
}
