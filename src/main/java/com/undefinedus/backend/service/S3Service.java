package com.undefinedus.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(String key, MultipartFile file);
//    byte[] downloadFile(String key);
    void deleteFile(String key);
    String getFileUrl(String key);
}
