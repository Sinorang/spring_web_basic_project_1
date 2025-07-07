package com.elice.boardproject.playlist;

import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.entity.PlaylistSong;
import com.elice.boardproject.playlist.repository.PlaylistRepository;
import com.elice.boardproject.playlist.repository.PlaylistSongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PlaylistRepositoryTest {
    @Autowired PlaylistRepository playlistRepository;
    @Autowired PlaylistSongRepository playlistSongRepository;

    @Test
    void 플레이리스트_및_곡_저장_조회() {
        // given
        Playlist playlist = new Playlist();
        playlist.setTitle("테스트 플레이리스트");
        playlist.setDescription("설명");
        playlist.setYoutubePlaylistId("PL123456");
        playlist.setCoverImageUrl("http://image.url");
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

        // when
        playlistRepository.save(playlist);
        // then
        List<Playlist> playlists = playlistRepository.findAll();
        assertThat(playlists).hasSize(1);
        Playlist found = playlists.get(0);
        assertThat(found.getSongs()).hasSize(2);
        assertThat(found.getSongs().get(0).getTitle()).isEqualTo("노래1");
        assertThat(found.getSongs().get(1).getTitle()).isEqualTo("노래2");
    }
} 