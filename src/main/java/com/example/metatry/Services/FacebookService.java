package com.example.metatry.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
@Service
@RequiredArgsConstructor
public class FacebookService {

    private final RestTemplate restTemplate;
    private final CloudinaryService cloudinaryService;

    @Value("${facebook.page-id:}")
    private String pageId;

    @Value("${facebook.page-access-token:}")
    private String token;

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v19.0/";

    public Map<String, Object> postText(String message) {

        String url = GRAPH_API_URL + pageId + "/feed";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("message", message);
        body.add("access_token", token);

        return restTemplate.postForObject(url, body, Map.class);
    }

    public Map<String, Object> postPhotoFromUrl(String imageUrl, String caption) {

        String url = GRAPH_API_URL + pageId + "/photos";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("url", imageUrl);
        body.add("caption", caption != null ? caption : "");
        body.add("access_token", token);

        return restTemplate.postForObject(url, body, Map.class);
    }

    public Map<String, Object> postLocalPhoto(MultipartFile file, String caption) {

        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return postPhotoFromUrl(imageUrl, caption);
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    public Map<String, Object> postVideoFromUrl(String videoUrl, String caption) {

        String url = GRAPH_API_URL + pageId + "/videos";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("file_url", videoUrl);
        body.add("description", caption != null ? caption : "");
        body.add("access_token", token);

        return restTemplate.postForObject(url, body, Map.class);
    }

    public Map<String, Object> postMultiplePhotos(List<String> imageUrls, String message) {

        try {
            List<String> mediaIds = new java.util.ArrayList<>();

            for (String imageUrl : imageUrls) {
                String photoUrl = GRAPH_API_URL + pageId + "/photos?published=false";

                MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                body.add("url", imageUrl);
                body.add("published", "false");
                body.add("access_token", token);

                Map photoRes = restTemplate.postForObject(photoUrl, body, Map.class);
                String mediaId = photoRes.get("id").toString();
                mediaIds.add(mediaId);
            }

            String feedUrl = GRAPH_API_URL + pageId + "/feed";

            StringBuilder attachedMedia = new StringBuilder("[");
            for (int i = 0; i < mediaIds.size(); i++) {
                if (i > 0) attachedMedia.append(",");
                attachedMedia.append("{\"media_fbid\":\"").append(mediaIds.get(i)).append("\"}");
            }
            attachedMedia.append("]");

            String jsonBody = "{\"message\":\"" + escape(message) + "\",\"attached_media\":" + attachedMedia + ",\"access_token\":\"" + token + "\"}";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            return restTemplate.exchange(feedUrl, HttpMethod.POST, entity, Map.class).getBody();

        } catch (Exception e) {
            System.out.println("Facebook postMultiplePhotos failed: " + e.getMessage());
            e.printStackTrace(System.out);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", " ");
    }
}