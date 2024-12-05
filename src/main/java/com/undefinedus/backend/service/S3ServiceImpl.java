package com.undefinedus.backend.service;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // 파일 업로드
    @Override
    public String uploadFile(String key, MultipartFile file) {

        try {
            byte[] fileData = file.getBytes();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .acl("public-read")
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return getFileUrl(key);
    }

    // 파일 삭제
    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    // 파일 경로 얻는 메서드
    @Override
    public String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    // 파일이름 + UUID 로 중복없는 key 생성
    @Override
    public String generateFileKey(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return UUID.randomUUID().toString() + extension;
    }

//    // 파일의 메타데이터(ETag) 추출
//    public String getS3ETag(String s3Url) {
//        String key = extractKeyFromUrl(s3Url);
//
//        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
//            .bucket(bucketName)
//            .key(key)
//            .build();
//
//        HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
//        return headObjectResponse.eTag();
//    }
//
//    // 기존 파일과 새로운 파일의 ETag 비교
//    public boolean isSameFile(MultipartFile newFile, String existingETag) throws IOException, NoSuchAlgorithmException {
//        MessageDigest digest = MessageDigest.getInstance("MD5");
//        byte[] hashBytes = digest.digest(newFile.getBytes());
//        String newFileHash = Base64.getEncoder().encodeToString(hashBytes);
//
//        return newFileHash.equals(existingETag);
//    }

    // S3 URL에서 Key를 추출하는 메서드
    @Override
    public String extractKeyFromUrl(String s3Url) {
        // URL 형식: https://<bucket-name>.s3.<region>.amazonaws.com/<key>
        int index = s3Url.indexOf(".amazonaws.com/");
        if (index == -1) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
        }
        return s3Url.substring(index + 14); // 14는 ".amazonaws.com/" 뒤를 의미
    }
}
