package com.elice.boardproject.playlist.service;

import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.repository.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    /**
     * playlistId로 YouTube API에서 곡 정보를 가져와 Playlist/PlaylistSong을 저장한다.
     * @param playlistId 유튜브 플레이리스트 ID
     * @return 저장된 Playlist
     */
    public Playlist createPlaylistFromYoutube(String playlistId) {
        // TODO: 실제 API 연동 및 저장 구현
        return null;
    }
} 