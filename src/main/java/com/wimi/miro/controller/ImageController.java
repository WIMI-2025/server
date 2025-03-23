package com.wimi.miro.controller;

import com.wimi.miro.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("user_uid") String userUid,
            @RequestParam("image_uid") String imageUid) {

        String imageUrl = storageService.uploadImage(file, userUid, imageUid);

        Map<String, String> response = new HashMap<>();
        response.put("image_url", imageUrl);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteImage(@RequestBody Map<String, String> request) {
        String userUid = request.get("user_uid");
        String imageUid = request.get("image_uid");

        if (userUid == null || imageUid == null) {
            throw new IllegalArgumentException("사용자 ID와 이미지 ID는 필수입니다.");
        }

        storageService.deleteImage(userUid, imageUid);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}