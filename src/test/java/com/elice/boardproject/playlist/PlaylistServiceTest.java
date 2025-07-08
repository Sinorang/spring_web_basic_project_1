package com.elice.boardproject.playlist;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.entity.PlaylistSong;
import com.elice.boardproject.playlist.repository.PlaylistRepository;
import com.elice.boardproject.playlist.repository.PlaylistSongRepository;
import com.elice.boardproject.playlist.service.PlaylistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlaylistServiceTest {
    @Autowired PlaylistRepository playlistRepository;
    @Autowired PlaylistSongRepository playlistSongRepository;
    @Autowired PlaylistService playlistService;
    @Autowired UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new User();
        testUser.setId("testuser");
        testUser.setName("테스트 사용자");
        testUser.setNickname("테스트닉");
        testUser.setEmail("test@example.com");
        testUser.setPwd("password");
        userRepository.save(testUser);
    }

    // 실제 API 호출 대신 mock 데이터 사용
    private static final String TEST_URL = "https://www.youtube.com/playlist?list=PL123456";
    private static final String TEST_PLAYLIST_ID = "PL123456";

    @Test
    void 유튜브_플레이리스트_URL_입력시_곡정보_저장_및_조회() {
        // given: 사용자가 URL을 입력
        String url = TEST_URL;
        String playlistId = extractPlaylistId(url);
        assertThat(playlistId).isEqualTo(TEST_PLAYLIST_ID);

        // when: (API 호출 대신) mock 곡 정보로 Playlist, PlaylistSong 저장
        Playlist playlist = new Playlist();
        playlist.setTitle("테스트 플레이리스트");
        playlist.setYoutubePlaylistId(playlistId);
        playlist.setPublic(true);

        PlaylistSong song1 = new PlaylistSong();
        song1.setTitle("노래1");
        song1.setArtist("아티스트1");
        song1.setYoutubeVideoId("vid1");
        song1.setOrderIndex(0);
        song1.setPlaylist(playlist);

        PlaylistSong song2 = new PlaylistSong();
        song2.setTitle("노래2");
        song2.setArtist("아티스트2");
        song2.setYoutubeVideoId("vid2");
        song2.setOrderIndex(1);
        song2.setPlaylist(playlist);

        playlist.getSongs().add(song1);
        playlist.getSongs().add(song2);
        playlistRepository.save(playlist);

        // then: 저장된 데이터 검증
        List<Playlist> playlists = playlistRepository.findAll();
        assertThat(playlists).hasSize(1);
        Playlist found = playlists.get(0);
        assertThat(found.getYoutubePlaylistId()).isEqualTo(TEST_PLAYLIST_ID);
        assertThat(found.getSongs()).hasSize(2);
        assertThat(found.getSongs().get(0).getTitle()).isEqualTo("노래1");
        assertThat(found.getSongs().get(1).getTitle()).isEqualTo("노래2");
    }

    @Test
    void YouTube_URL로_플레이리스트_생성_성공() {
        // given
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        
        // when
        Playlist createdPlaylist = playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);
        
        // then
        assertThat(createdPlaylist).isNotNull();
        assertThat(createdPlaylist.getYoutubePlaylistId()).isEqualTo("PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj");
        assertThat(createdPlaylist.getOwner()).isEqualTo(testUser);
        assertThat(createdPlaylist.getTitle()).isNotEmpty();
        assertThat(createdPlaylist.getSongs()).isNotEmpty();
    }

    @Test
    void 잘못된_YouTube_URL로_플레이리스트_생성_실패() {
        // given
        String invalidUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        
        // when & then
        assertThat(playlistService.createPlaylistFromYoutubeUrl(invalidUrl, testUser)).isNull();
    }

    @Test
    void 사용자별_플레이리스트_조회_성공() {
        // given
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);
        
        // when
        List<Playlist> userPlaylists = playlistService.getPlaylistsByUser(testUser);
        
        // then
        assertThat(userPlaylists).isNotEmpty();
        assertThat(userPlaylists.get(0).getOwner()).isEqualTo(testUser);
    }

    @Test
    void 플레이리스트_ID로_상세_조회_성공() {
        // given
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        Playlist createdPlaylist = playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);
        
        // when
        Playlist foundPlaylist = playlistService.getPlaylistById(createdPlaylist.getId());
        
        // then
        assertThat(foundPlaylist).isNotNull();
        assertThat(foundPlaylist.getId()).isEqualTo(createdPlaylist.getId());
        assertThat(foundPlaylist.getSongs()).isNotEmpty();
    }

    @Test
    void 플레이리스트_삭제_성공() {
        // given
        String youtubeUrl = "https://music.youtube.com/playlist?list=PLdWdCc1yLsnH-iCVckG-l0EV5APQGi4jj&si=umG4km0OVifUNSLw";
        Playlist createdPlaylist = playlistService.createPlaylistFromYoutubeUrl(youtubeUrl, testUser);
        
        // when
        playlistService.deletePlaylist(createdPlaylist.getId(), testUser);
        
        // then
        Playlist deletedPlaylist = playlistService.getPlaylistById(createdPlaylist.getId());
        assertThat(deletedPlaylist).isNull();
    }

    @Test
    void playlistId로_API_연동_및_저장_테스트() {
        // given
        String playlistId = "PL123456";
        // when (실제 API 연동 전, mock/stub)
        Playlist playlist = playlistService.createPlaylistFromYoutube(playlistId);
        // then (아직 null 반환, 추후 구현 시 검증 추가)
        assertThat(playlist).isNull();
    }

    // 실제 구현에서는 더 robust하게 처리해야 함
    private String extractPlaylistId(String url) {
        int idx = url.indexOf("list=");
        if (idx == -1) return null;
        return url.substring(idx + 5);
    }
} 