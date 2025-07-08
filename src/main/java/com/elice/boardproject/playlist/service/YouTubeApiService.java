package com.elice.boardproject.playlist.service;

import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
public class YouTubeApiService {

    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("^PL[a-zA-Z0-9_-]{10,}$");

    /**
     * YouTube 플레이리스트 URL에서 플레이리스트 ID를 추출합니다.
     * 
     * @param url YouTube 플레이리스트 URL
     * @return 플레이리스트 ID
     * @throws IllegalArgumentException URL이 유효하지 않은 경우
     */
    public String extractPlaylistId(String url) {
        ExceptionUtils.requireNonEmpty(url, ErrorCode.INVALID_YOUTUBE_URL, url);

        // YouTube 플레이리스트 URL 패턴들
        String[] patterns = {
            "list=([a-zA-Z0-9_-]+)",
            "playlist\\?list=([a-zA-Z0-9_-]+)"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(url);
            
            if (m.find()) {
                String playlistId = m.group(1);
                if (isValidPlaylistId(playlistId)) {
                    return playlistId;
                }
            }
        }

        ExceptionUtils.throwException(ErrorCode.INVALID_PLAYLIST_URL, url);
        return null; // 이 라인은 실행되지 않음
    }

    /**
     * 플레이리스트 ID의 유효성을 검증합니다.
     * 
     * @param playlistId 플레이리스트 ID
     * @return 유효한 경우 true, 그렇지 않은 경우 false
     */
    public boolean isValidPlaylistId(String playlistId) {
        if (!StringUtils.hasText(playlistId)) {
            return false;
        }
        
        return PLAYLIST_ID_PATTERN.matcher(playlistId).matches();
    }
} 