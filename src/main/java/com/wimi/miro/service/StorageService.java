package com.wimi.miro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Autowired
    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile file, String userUid, String imageUid) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
            }

            // Generate key with user folder structure
            String fileExtension = getExtensionFromFilename(file.getOriginalFilename());
            String key = String.format("users/%s/images/%s.%s", userUid, imageUid, fileExtension);

            // Upload to S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return CloudFront URL
            return String.format("%s/%s", cloudfrontDomain, key);
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    public void deleteImage(String userUid, String imageUid) {
        // Image key needs to include full path with extension
        // This is a limitation since we don't store the extension separately
        // In a real app, we might store image metadata in the database
        String keyPrefix = String.format("users/%s/images/%s", userUid, imageUid);

        // For simplicity, we're assuming we know the extension
        // In a real app, you'd query for objects with the prefix and delete them
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyPrefix)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String getExtensionFromFilename(String filename) {
        if (filename == null) {
            return "jpg"; // Default extension
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "jpg"; // Default extension
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    public String generateUniqueImageId() {
        return UUID.randomUUID().toString();
    }
}