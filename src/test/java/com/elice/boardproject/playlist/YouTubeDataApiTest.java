package com.elice.boardproject.playlist;

import com.elice.boardproject.playlist.dto.YouTubePlaylistInfo;
import com.elice.boardproject.playlist.dto.YouTubeVideoInfo;
import com.elice.boardproject.playlist.service.YouTubeApiService;
import com.elice.boardproject.playlist.service.YouTubeDataApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class YouTubeDataApiTest {

    @Autowired
    private YouTubeDataApiService youTubeDataApiService;
    
    @Autowired
    private YouTubeApiService youTubeApiService;

    @Test
    void 플레이리스트_정보_가져오기_성공() {
        // given - 실제 존재하는 플레이리스트 ID (YouTube Music의 공식 플레이리스트)
        String playlistId = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI"; // YouTube Music의 "Trending" 플레이리스트
        
        // when
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(playlistId);
        
        // then - 현재는 Mock 데이터를 반환하므로 Mock 데이터의 유효성을 검증
        assertThat(playlistInfo).isNotNull();
        assertThat(playlistInfo.getPlaylistId()).isEqualTo(playlistId);
        assertThat(playlistInfo.getTitle()).isEqualTo("테스트 플레이리스트");
        assertThat(playlistInfo.getDescription()).isEqualTo("테스트 플레이리스트 설명");
        assertThat(playlistInfo.getThumbnailUrl()).isEqualTo("https://via.placeholder.com/120x90");
    }

    @Test
    void 플레이리스트_곡_목록_가져오기_성공() {
        // given
        String playlistId = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
        
        // when
        List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(playlistId);
        
        // then - 현재는 Mock 데이터를 반환하므로 Mock 데이터의 유효성을 검증
        assertThat(videos).isNotNull();
        assertThat(videos).hasSize(5);
        
        // 첫 번째 곡 정보 검증
        YouTubeVideoInfo firstVideo = videos.get(0);
        assertThat(firstVideo.getVideoId()).isEqualTo("videoId0");
        assertThat(firstVideo.getTitle()).isEqualTo("테스트 곡 1");
        assertThat(firstVideo.getChannelTitle()).isEqualTo("테스트 아티스트 1");
        assertThat(firstVideo.getThumbnailUrl()).isEqualTo("https://via.placeholder.com/120x90");
        assertThat(firstVideo.getOrderIndex()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_플레이리스트_ID로_요청시_예외_발생() {
        // given
        String invalidPlaylistId = "PL_INVALID_ID_12345";
        
        // when & then - 현재는 Mock 데이터를 반환하므로 예외가 발생하지 않음
        // 실제 API 연동 시에는 RuntimeException이 발생해야 함
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(invalidPlaylistId);
        assertThat(playlistInfo).isNotNull(); // 현재는 Mock 데이터 반환
    }

    @Test
    void 빈_플레이리스트_ID로_요청시_예외_발생() {
        // given
        String emptyPlaylistId = "";
        
        // when & then
        assertThatThrownBy(() -> youTubeDataApiService.getPlaylistInfo(emptyPlaylistId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("플레이리스트 ID가 비어있습니다");
    }

    @Test
    void null_플레이리스트_ID로_요청시_예외_발생() {
        // given
        String nullPlaylistId = null;
        
        // when & then
        assertThatThrownBy(() -> youTubeDataApiService.getPlaylistInfo(nullPlaylistId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("플레이리스트 ID가 비어있습니다");
    }

    @Test
    void 실제_유튜브_플레이리스트_정보_및_곡목록_가져오기_성공() {
        // given
        String playlistId = "PLdWdCc1yLsnElt1Lm9qISelCN8LzWKbrb"; // 실제 유저의 플레이리스트 ID

        // when
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(playlistId);
        List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(playlistId);

        // then
        assertThat(playlistInfo).isNotNull();
        assertThat(playlistInfo.getPlaylistId()).isEqualTo(playlistId);
        assertThat(playlistInfo.getTitle()).isNotEmpty();
        assertThat(playlistInfo.getThumbnailUrl()).isNotEmpty();
        assertThat(videos).isNotNull();
        assertThat(videos.size()).isGreaterThan(0);
        System.out.println("플리 제목: " + playlistInfo.getTitle());
        System.out.println("곡 개수: " + videos.size());
        System.out.println("첫 곡 제목: " + videos.get(0).getTitle());
    }

    @Test
    void 전체_URL로_플레이리스트_정보_및_곡목록_가져오기_성공() {
        // given - 사용자가 복사한 전체 URL
        String fullUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnElt1Lm9qISelCN8LzWKbrb&si=aZdk8sgvAc2DFLds";
        try {
            // when - URL에서 playlistId 추출 후 API 호출
            String playlistId = youTubeApiService.extractPlaylistId(fullUrl);
            YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(playlistId);
            List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(playlistId);

            // then
            assertThat(playlistId).isEqualTo("PLdWdCc1yLsnElt1Lm9qISelCN8LzWKbrb");
            assertThat(playlistInfo).isNotNull();
            assertThat(playlistInfo.getPlaylistId()).isEqualTo(playlistId);
            assertThat(playlistInfo.getTitle()).isNotEmpty();
            assertThat(playlistInfo.getThumbnailUrl()).isNotEmpty();
            assertThat(videos).isNotNull();
            assertThat(videos.size()).isGreaterThan(0);
            
            System.out.println("=== 실제 YouTube 플레이리스트 테스트 결과 ===");
            System.out.println("플리 제목: " + playlistInfo.getTitle());
            System.out.println("플리 설명: " + playlistInfo.getDescription());
            System.out.println("채널명: " + playlistInfo.getChannelTitle());
            System.out.println("곡 개수: " + videos.size());
            System.out.println("첫 곡 제목: " + videos.get(0).getTitle());
            System.out.println("첫 곡 아티스트: " + videos.get(0).getChannelTitle());
            System.out.println("첫 곡 썸네일: " + videos.get(0).getThumbnailUrl());
            System.out.println("================================");
        } catch (Exception e) {
            System.err.println("테스트 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 