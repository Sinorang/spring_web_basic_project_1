package com.elice.boardproject.playlist;

import com.elice.boardproject.playlist.service.YouTubeApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.elice.boardproject.exception.PliException;
import com.elice.boardproject.exception.ErrorCode;

@SpringBootTest
@ActiveProfiles("test")
class YouTubeApiServiceTest {

    @Autowired
    private YouTubeApiService youTubeApiService;

    @Test
    void 유튜브_플레이리스트_URL에서_플레이리스트_ID_추출_성공() {
        // given
        String validUrl = "https://www.youtube.com/playlist?list=PL1234567890abcdef";
        
        // when
        String playlistId = youTubeApiService.extractPlaylistId(validUrl);
        
        // then
        assertThat(playlistId).isEqualTo("PL1234567890abcdef");
    }

    @Test
    void 유튜브_뮤직_플레이리스트_URL에서_플레이리스트_ID_추출_성공() {
        // given
        String musicUrl = "https://music.youtube.com/playlist?list=PLabcdef123456789";
        
        // when
        String playlistId = youTubeApiService.extractPlaylistId(musicUrl);
        
        // then
        assertThat(playlistId).isEqualTo("PLabcdef123456789");
    }

    @Test
    void 실제_유튜브_뮤직_플레이리스트_URL에서_플레이리스트_ID_추출_성공() {
        // given - 실제 YouTube Music 플레이리스트 URL
        String realMusicUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        
        // when
        String playlistId = youTubeApiService.extractPlaylistId(realMusicUrl);
        
        // then
        assertThat(playlistId).isEqualTo("PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj");
    }

    @Test
    void 잘못된_URL에서_플레이리스트_ID_추출_실패() {
        // given
        String invalidUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        // when & then
        assertThatThrownBy(() -> youTubeApiService.extractPlaylistId(invalidUrl))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PLAYLIST_URL);
    }

    @Test
    void 빈_URL에서_플레이리스트_ID_추출_실패() {
        // given
        String emptyUrl = "";
        
        // when & then
        assertThatThrownBy(() -> youTubeApiService.extractPlaylistId(emptyUrl))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_YOUTUBE_URL);
    }

    @Test
    void null_URL에서_플레이리스트_ID_추출_실패() {
        // given
        String nullUrl = null;
        
        // when & then
        assertThatThrownBy(() -> youTubeApiService.extractPlaylistId(nullUrl))
                .isInstanceOf(PliException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_YOUTUBE_URL);
    }

    @Test
    void 플레이리스트_ID_유효성_검증_성공() {
        // given
        String validPlaylistId = "PL1234567890abcdef";
        
        // when
        boolean isValid = youTubeApiService.isValidPlaylistId(validPlaylistId);
        
        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void 실제_플레이리스트_ID_유효성_검증_성공() {
        // given - 실제 YouTube Music 플레이리스트 ID
        String realPlaylistId = "PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj";
        
        // when
        boolean isValid = youTubeApiService.isValidPlaylistId(realPlaylistId);
        
        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void 잘못된_플레이리스트_ID_유효성_검증_실패() {
        // given
        String invalidPlaylistId = "INVALID_ID";
        
        // when
        boolean isValid = youTubeApiService.isValidPlaylistId(invalidPlaylistId);
        
        // then
        assertThat(isValid).isFalse();
    }
} 