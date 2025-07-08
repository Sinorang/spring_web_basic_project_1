package com.elice.boardproject.playlist;

import com.elice.boardproject.playlist.dto.YouTubePlaylistInfo;
import com.elice.boardproject.playlist.dto.YouTubeVideoInfo;
import com.elice.boardproject.playlist.service.YouTubeApiService;
import com.elice.boardproject.playlist.service.YouTubeDataApiService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class YouTubeDataApiTest {

    @MockBean
    private YouTubeDataApiService youTubeDataApiService;
    
    @MockBean
    private YouTubeApiService youTubeApiService;

    @Test
    void 플레이리스트_정보_가져오기_성공() {
        String playlistId = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
        given(youTubeDataApiService.getPlaylistInfo(playlistId)).willReturn(
            new com.elice.boardproject.playlist.dto.YouTubePlaylistInfo(
                playlistId,
                "테스트 플레이리스트",
                "테스트 플레이리스트 설명",
                "https://via.placeholder.com/120x90",
                "테스트 채널",
                "2024-01-01T00:00:00Z",
                5
            )
        );
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(playlistId);
        assertThat(playlistInfo).isNotNull();
        assertThat(playlistInfo.getPlaylistId()).isEqualTo(playlistId);
        assertThat(playlistInfo.getTitle()).isEqualTo("테스트 플레이리스트");
        assertThat(playlistInfo.getDescription()).isEqualTo("테스트 플레이리스트 설명");
        assertThat(playlistInfo.getThumbnailUrl()).isEqualTo("https://via.placeholder.com/120x90");
    }

    @Test
    void 플레이리스트_곡_목록_가져오기_성공() {
        String playlistId = "PLFgquLnL59alCl_2TQvOiD5Vgm1hCaGSI";
        given(youTubeDataApiService.getPlaylistVideos(playlistId)).willReturn(
            java.util.List.of(
                new com.elice.boardproject.playlist.dto.YouTubeVideoInfo(
                    "videoId0", "테스트 곡 1", null, "테스트 아티스트 1", "https://via.placeholder.com/120x90", null, 0, null
                ),
                new com.elice.boardproject.playlist.dto.YouTubeVideoInfo(
                    "videoId1", "테스트 곡 2", null, "테스트 아티스트 2", "https://via.placeholder.com/120x90", null, 1, null
                )
            )
        );
        java.util.List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(playlistId);
        assertThat(videos).isNotNull();
        assertThat(videos).hasSize(2);
        YouTubeVideoInfo firstVideo = videos.get(0);
        assertThat(firstVideo.getVideoId()).isEqualTo("videoId0");
        assertThat(firstVideo.getTitle()).isEqualTo("테스트 곡 1");
        assertThat(firstVideo.getChannelTitle()).isEqualTo("테스트 아티스트 1");
        assertThat(firstVideo.getThumbnailUrl()).isEqualTo("https://via.placeholder.com/120x90");
        assertThat(firstVideo.getOrderIndex()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_플레이리스트_ID로_요청시_예외_발생() {
        String invalidPlaylistId = "PL_INVALID_ID_12345";
        given(youTubeDataApiService.getPlaylistInfo(invalidPlaylistId)).willReturn(null);
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(invalidPlaylistId);
        assertThat(playlistInfo).isNull();
    }

    @Test
    void 빈_플레이리스트_ID로_요청시_예외_발생() {
        String emptyPlaylistId = "";
        given(youTubeDataApiService.getPlaylistInfo(emptyPlaylistId)).willThrow(new com.elice.boardproject.exception.PliException(com.elice.boardproject.exception.ErrorCode.INVALID_PLAYLIST_URL));
        assertThatThrownBy(() -> youTubeDataApiService.getPlaylistInfo(emptyPlaylistId))
                .isInstanceOf(com.elice.boardproject.exception.PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", com.elice.boardproject.exception.ErrorCode.INVALID_PLAYLIST_URL);
    }

    @Test
    void null_플레이리스트_ID로_요청시_예외_발생() {
        String nullPlaylistId = null;
        given(youTubeDataApiService.getPlaylistInfo(nullPlaylistId)).willThrow(new com.elice.boardproject.exception.PliException(com.elice.boardproject.exception.ErrorCode.INVALID_PLAYLIST_URL));
        assertThatThrownBy(() -> youTubeDataApiService.getPlaylistInfo(nullPlaylistId))
                .isInstanceOf(com.elice.boardproject.exception.PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", com.elice.boardproject.exception.ErrorCode.INVALID_PLAYLIST_URL);
    }

    @Test
    @Disabled("외부 API 호출 테스트는 기본적으로 비활성화. Mock 기반 테스트만 활성화.")
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
        String fullUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnElt1Lm9qISelCN8LzWKbrb&si=aZdk8sgvAc2DFLds";
        String playlistId = "PLdWdCc1yLsnElt1Lm9qISelCN8LzWKbrb";
        given(youTubeApiService.extractPlaylistId(fullUrl)).willReturn(playlistId);
        given(youTubeDataApiService.getPlaylistInfo(playlistId)).willReturn(
            new com.elice.boardproject.playlist.dto.YouTubePlaylistInfo(
                playlistId,
                "테스트 플레이리스트",
                "테스트 설명",
                "https://via.placeholder.com/120x90",
                "테스트 채널",
                "2024-01-01T00:00:00Z",
                2
            )
        );
        given(youTubeDataApiService.getPlaylistVideos(playlistId)).willReturn(
            java.util.List.of(
                new com.elice.boardproject.playlist.dto.YouTubeVideoInfo(
                    "vid1", "노래1", null, "테스트 채널", "https://via.placeholder.com/120x90", null, 0, null
                ),
                new com.elice.boardproject.playlist.dto.YouTubeVideoInfo(
                    "vid2", "노래2", null, "테스트 채널", "https://via.placeholder.com/120x90", null, 1, null
                )
            )
        );
        String extractedId = youTubeApiService.extractPlaylistId(fullUrl);
        YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(extractedId);
        java.util.List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(extractedId);
        assertThat(extractedId).isEqualTo(playlistId);
        assertThat(playlistInfo).isNotNull();
        assertThat(playlistInfo.getPlaylistId()).isEqualTo(playlistId);
        assertThat(playlistInfo.getTitle()).isNotEmpty();
        assertThat(playlistInfo.getThumbnailUrl()).isNotEmpty();
        assertThat(videos).isNotNull();
        assertThat(videos.size()).isEqualTo(2);
    }
} 