package com.wimi.miro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wimi.miro.config.OpenAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Base64;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final OpenAIConfig geminiConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiService(WebClient geminiWebClient, OpenAIConfig geminiConfig, ObjectMapper objectMapper) {
        this.webClient = geminiWebClient;
        this.geminiConfig = geminiConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * 텍스트 프롬프트로 Gemini API 호출
     */
    public String generateContent(String systemPrompt, String userPrompt) {
        try {
            // Request body 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();

            // System prompt 추가 (역할 정의)
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemContent = objectMapper.createObjectNode();
                ArrayNode systemParts = objectMapper.createArrayNode();
                ObjectNode systemTextPart = objectMapper.createObjectNode();
                systemTextPart.put("text", systemPrompt);
                systemParts.add(systemTextPart);
                systemContent.put("role", "system");
                systemContent.set("parts", systemParts); // 배열 노드이므로 set 사용
                contents.add(systemContent);
            }

            // User prompt 추가
            ObjectNode userContent = objectMapper.createObjectNode();
            ArrayNode userParts = objectMapper.createArrayNode();
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", userPrompt);
            userParts.add(textPart);
            userContent.put("role", "user");
            userContent.set("parts", userParts); // 배열 노드이므로 set 사용
            contents.add(userContent);

            requestBody.set("contents", contents); // 배열 노드이므로 set 사용
            ObjectNode genConfig = objectMapper.createObjectNode();
            genConfig.put("temperature", 0.4);
            genConfig.put("topK", 32);
            genConfig.put("topP", 1.0);
            genConfig.put("maxOutputTokens", 8192);
            requestBody.set("generationConfig", genConfig);

            // API 호출
            JsonNode response = webClient.post()
                    .uri(geminiConfig.getGenerateContentEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // 응답에서 텍스트 추출
            if (response != null && response.has("candidates") && response.get("candidates").isArray()) {
                JsonNode candidate = response.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")
                        && candidate.get("content").get("parts").isArray()) {
                    JsonNode part = candidate.get("content").get("parts").get(0);
                    if (part.has("text")) {
                        return part.get("text").asText();
                    }
                }
            }

            return "응답을 처리하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            throw new RuntimeException("Gemini API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지와 텍스트로 Gemini API 호출
     */
    public String generateContentWithImage(String systemPrompt, String userPrompt, MultipartFile image) {
        try {
            // 이미지를 Base64로 인코딩
            byte[] imageBytes = image.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = image.getContentType();

            // Request body 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();

            // System prompt 추가 (역할 정의)
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemContent = objectMapper.createObjectNode();
                ArrayNode systemParts = objectMapper.createArrayNode();
                ObjectNode systemTextPart = objectMapper.createObjectNode();
                systemTextPart.put("text", systemPrompt);
                systemParts.add(systemTextPart);
                systemContent.put("role", "system");
                systemContent.set("parts", systemParts);
                contents.add(systemContent);
            }

            // User prompt with image 추가
            ObjectNode userContent = objectMapper.createObjectNode();
            ArrayNode userParts = objectMapper.createArrayNode();

            // 텍스트 부분 추가
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", userPrompt);
            userParts.add(textPart);

            // 이미지 부분 추가
            ObjectNode imagePart = objectMapper.createObjectNode();
            ObjectNode inlineDataNode = objectMapper.createObjectNode();
            inlineDataNode.put("mime_type", mimeType);
            inlineDataNode.put("data", base64Image);
            imagePart.set("inline_data", inlineDataNode);
            userParts.add(imagePart);

            userContent.put("role", "user");
            userContent.set("parts", userParts);
            contents.add(userContent);

            requestBody.set("contents", contents);
            requestBody.set("generationConfig", objectMapper.createObjectNode()
                    .put("temperature", 0.4)
                    .put("topK", 32)
                    .put("topP", 1.0)
                    .put("maxOutputTokens", 8192));

            // API 호출
            JsonNode response = webClient.post()
                    .uri(geminiConfig.getGenerateContentEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // 응답에서 텍스트 추출
            if (response != null && response.has("candidates") && response.get("candidates").isArray()) {
                JsonNode candidate = response.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")
                        && candidate.get("content").get("parts").isArray()) {
                    JsonNode part = candidate.get("content").get("parts").get(0);
                    if (part.has("text")) {
                        return part.get("text").asText();
                    }
                }
            }

            return "응답을 처리하는 중 오류가 발생했습니다.";
        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 이미지 URL로 Gemini API 호출
     */
    public String generateContentWithImageUrl(String systemPrompt, String userPrompt, String imageUrl) {
        try {
            // Request body 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();

            // System prompt 추가 (역할 정의)
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemContent = objectMapper.createObjectNode();
                ArrayNode systemParts = objectMapper.createArrayNode();
                ObjectNode systemTextPart = objectMapper.createObjectNode();
                systemTextPart.put("text", systemPrompt);
                systemParts.add(systemTextPart);
                systemContent.put("role", "system");
                systemContent.set("parts", systemParts);
                contents.add(systemContent);
            }

            // User prompt with image URL 추가
            ObjectNode userContent = objectMapper.createObjectNode();
            ArrayNode userParts = objectMapper.createArrayNode();

            // 텍스트 부분 추가
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", userPrompt);
            userParts.add(textPart);

            // 이미지 URL 부분 추가
            ObjectNode imagePart = objectMapper.createObjectNode();
            ObjectNode fileDataNode = objectMapper.createObjectNode();
            fileDataNode.put("mime_type", "image/jpeg"); // MIME 타입 추정
            fileDataNode.put("file_uri", imageUrl);
            imagePart.set("file_data", fileDataNode);
            userParts.add(imagePart);

            userContent.put("role", "user");
            userContent.set("parts", userParts);
            contents.add(userContent);

            requestBody.set("contents", contents);
            requestBody.put("generationConfig", objectMapper.createObjectNode()
                    .put("temperature", 0.4)
                    .put("topK", 32)
                    .put("topP", 1.0)
                    .put("maxOutputTokens", 8192));

            // API 호출
            JsonNode response = webClient.post()
                    .uri(geminiConfig.getGenerateContentEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // 응답에서 텍스트 추출
            if (response != null && response.has("candidates") && response.get("candidates").isArray()) {
                JsonNode candidate = response.get("candidates").get(0);
                if (candidate.has("content") && candidate.get("content").has("parts")
                        && candidate.get("content").get("parts").isArray()) {
                    JsonNode part = candidate.get("content").get("parts").get(0);
                    if (part.has("text")) {
                        return part.get("text").asText();
                    }
                }
            }

            return "응답을 처리하는 중 오류가 발생했습니다.";
        } catch (Exception e) {
            throw new RuntimeException("Gemini API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}