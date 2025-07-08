package com.elice.boardproject.playlist.service;

import com.elice.boardproject.playlist.dto.YouTubePlaylistInfo;
import com.elice.boardproject.playlist.dto.YouTubeVideoInfo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class YouTubeDataApiService {

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

        // TODO: 실제 YouTube API 연동 구현
        // 현재는 Mock 데이터 반환
        return new YouTubePlaylistInfo(
                playlistId,
                "테스트 플레이리스트",
                "테스트 플레이리스트 설명",
                "https://via.placeholder.com/120x90",
                "테스트 채널",
                "2024-01-01T00:00:00Z",
                5
        );
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

        // TODO: 실제 YouTube API 연동 구현
        // 현재는 Mock 데이터 반환
        List<YouTubeVideoInfo> videos = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            videos.add(new YouTubeVideoInfo(
                    "videoId" + i,
                    "테스트 곡 " + (i + 1),
                    "테스트 곡 설명 " + (i + 1),
                    "테스트 아티스트 " + (i + 1),
                    "https://via.placeholder.com/120x90",
                    "2024-01-01T00:00:00Z",
                    i,
                    "3:30"
            ));
        }

        return videos;
    }
} 