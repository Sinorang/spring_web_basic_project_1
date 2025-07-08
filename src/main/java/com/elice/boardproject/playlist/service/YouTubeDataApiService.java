package com.elice.boardproject.playlist.service;

import com.elice.boardproject.playlist.dto.YouTubePlaylistInfo;
import com.elice.boardproject.playlist.dto.YouTubeVideoInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeDataApiService {

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";
    
    @Value("${youtube.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YouTubeDataApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 플레이리스트 정보를 가져옵니다.
     * 
     * @param playlistId 플레이리스트 ID
     * @return 플레이리스트 정보
     * @throws RuntimeException API 호출 실패 시
     */
    public YouTubePlaylistInfo getPlaylistInfo(String playlistId) {
        if (!StringUtils.hasText(playlistId)) {
            throw new IllegalArgumentException("플레이리스트 ID가 비어있습니다");
        }

        try {
            String url = String.format("%s/playlists?part=snippet,contentDetails&id=%s&key=%s",
                    YOUTUBE_API_BASE_URL, playlistId, apiKey);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("error")) {
                throw new RuntimeException("YouTube API 오류: " + rootNode.get("error").get("message").asText());
            }

            JsonNode items = rootNode.get("items");
            if (items == null || items.isEmpty()) {
                throw new RuntimeException("플레이리스트를 찾을 수 없습니다: " + playlistId);
            }

            JsonNode playlist = items.get(0);
            JsonNode snippet = playlist.get("snippet");
            JsonNode contentDetails = playlist.get("contentDetails");

            return new YouTubePlaylistInfo(
                    playlistId,
                    snippet.get("title").asText(),
                    snippet.get("description").asText(),
                    snippet.get("thumbnails").get("default").get("url").asText(),
                    snippet.get("channelTitle").asText(),
                    snippet.get("publishedAt").asText(),
                    contentDetails.get("itemCount").asInt()
            );

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.out.println("API 오류 응답: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("YouTube API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 플레이리스트의 비디오 목록을 가져옵니다.
     * 
     * @param playlistId 플레이리스트 ID
     * @return 비디오 목록
     * @throws RuntimeException API 호출 실패 시
     */
    public List<YouTubeVideoInfo> getPlaylistVideos(String playlistId) {
        if (!StringUtils.hasText(playlistId)) {
            throw new IllegalArgumentException("플레이리스트 ID가 비어있습니다");
        }

        try {
            List<YouTubeVideoInfo> videos = new ArrayList<>();
            String nextPageToken = null;

            do {
                // 플레이리스트 아이템 요청
                String playlistUrl = String.format("%s/playlistItems?part=snippet&playlistId=%s&maxResults=50&key=%s",
                        YOUTUBE_API_BASE_URL, playlistId, apiKey);

                if (nextPageToken != null) {
                    playlistUrl += "&pageToken=" + nextPageToken;
                }

                String playlistResponse = restTemplate.getForObject(playlistUrl, String.class);
                JsonNode playlistRoot = objectMapper.readTree(playlistResponse);

                if (playlistRoot.has("error")) {
                    throw new RuntimeException("YouTube API 오류: " + playlistRoot.get("error").get("message").asText());
                }

                JsonNode playlistItems = playlistRoot.get("items");
                if (playlistItems != null && playlistItems.size() > 0) {
                    // 비디오 ID 목록 추출
                    List<String> videoIds = new ArrayList<>();
                    for (JsonNode item : playlistItems) {
                        String videoId = item.get("snippet").get("resourceId").get("videoId").asText();
                        videoIds.add(videoId);
                    }

                    // 비디오 상세 정보 요청
                    String videoIdsParam = String.join(",", videoIds);
                    String videoUrl = String.format("%s/videos?part=snippet,contentDetails&id=%s&key=%s",
                            YOUTUBE_API_BASE_URL, videoIdsParam, apiKey);

                    String videoResponse = restTemplate.getForObject(videoUrl, String.class);
                    JsonNode videoRoot = objectMapper.readTree(videoResponse);

                    if (videoRoot.has("error")) {
                        throw new RuntimeException("YouTube API 오류: " + videoRoot.get("error").get("message").asText());
                    }

                    JsonNode videoItems = videoRoot.get("items");
                    if (videoItems != null) {
                        for (JsonNode video : videoItems) {
                            JsonNode snippet = video.get("snippet");
                            JsonNode contentDetails = video.get("contentDetails");

                            videos.add(new YouTubeVideoInfo(
                                    video.get("id").asText(),
                                    snippet.get("title").asText(),
                                    snippet.get("description").asText(),
                                    snippet.get("channelTitle").asText(),
                                    snippet.get("thumbnails").get("default").get("url").asText(),
                                    snippet.get("publishedAt").asText(),
                                    videos.size(), // 순서 인덱스
                                    contentDetails.get("duration").asText() // ISO 8601 형식 (PT4M13S)
                            ));
                        }
                    }
                }

                nextPageToken = playlistRoot.has("nextPageToken") ? 
                        playlistRoot.get("nextPageToken").asText() : null;

            } while (nextPageToken != null);

            return videos;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            System.out.println("API 오류 응답: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("YouTube API 호출 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
} 