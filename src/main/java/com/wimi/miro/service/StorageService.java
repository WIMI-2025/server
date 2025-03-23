package com.wimi.miro.service;

import com.wimi.miro.dto.response.ImageUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.awt.*;
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

    // 이미지 파일 업로드 메서드
    public ImageUploadResponse uploadImage(MultipartFile file, String userUid, String imageUid) {
        try {
            // 파일이 비어 있는지 확인
            if (file.isEmpty()) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }

            // 파일의 Content-Type이 이미지인지 확인
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
            }

            // 파일 확장자 추출 및 S3 저장 경로 생성
            String fileExtension = getExtensionFromFilename(file.getOriginalFilename());
            String key = String.format("users/%s/images/%s.%s", userUid, imageUid, fileExtension);

            // S3에 이미지 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            // S3에 실제 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 업로드된 이미지의 CloudFront 경로 반환
            String imageUrl = String.format("%s/%s", cloudfrontDomain, key);

            return ImageUploadResponse.builder()
                    .imageUrl(imageUrl)
                    .build();



        } catch (IOException e) {
            // 업로드 중 예외 발생 시 래핑하여 던짐
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다.", e);
        }
    }

    // 이미지 삭제 메서드
    public void deleteImage(String userUid, String imageUid) {
        // 이미지가 저장된 S3 key 생성
        String keyPrefix = String.format("users/%s/images/%s", userUid, imageUid);

        // S3 객체 삭제 요청 생성
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(keyPrefix)
                .build();

        // S3에서 객체 삭제
        s3Client.deleteObject(deleteObjectRequest);
    }

    // 파일 이름에서 확장자 추출 (없으면 기본값 "jpg" 반환)
    private String getExtensionFromFilename(String filename) {
        if (filename == null) {
            return "jpg"; // 기본 확장자
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "jpg"; // 확장자가 없거나 이상한 경우
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

}
