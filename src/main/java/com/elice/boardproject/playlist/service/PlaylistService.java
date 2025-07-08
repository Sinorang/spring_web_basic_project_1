package com.elice.boardproject.playlist.service;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.playlist.dto.YouTubePlaylistInfo;
import com.elice.boardproject.playlist.dto.YouTubeVideoInfo;
import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.entity.PlaylistSong;
import com.elice.boardproject.playlist.repository.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final YouTubeApiService youTubeApiService;
    private final YouTubeDataApiService youTubeDataApiService;

    public PlaylistService(PlaylistRepository playlistRepository, 
                          YouTubeApiService youTubeApiService,
                          YouTubeDataApiService youTubeDataApiService) {
        this.playlistRepository = playlistRepository;
        this.youTubeApiService = youTubeApiService;
        this.youTubeDataApiService = youTubeDataApiService;
    }

    /**
     * YouTube URL로 플레이리스트를 생성합니다.
     * @param youtubeUrl YouTube 플레이리스트 URL
     * @param owner 플레이리스트 소유자
     * @return 생성된 Playlist
     */
    public Playlist createPlaylistFromYoutubeUrl(String youtubeUrl, User owner) {
        try {
            // 1. URL에서 플레이리스트 ID 추출
            String playlistId = youTubeApiService.extractPlaylistId(youtubeUrl);
            
            // 2. YouTube API에서 플레이리스트 정보 가져오기
            YouTubePlaylistInfo playlistInfo = youTubeDataApiService.getPlaylistInfo(playlistId);
            List<YouTubeVideoInfo> videos = youTubeDataApiService.getPlaylistVideos(playlistId);
            
            // 3. Playlist 엔티티 생성
            Playlist playlist = new Playlist();
            playlist.setTitle(playlistInfo.getTitle());
            playlist.setDescription(playlistInfo.getDescription());
            playlist.setYoutubePlaylistId(playlistInfo.getPlaylistId());
            playlist.setCoverImageUrl(playlistInfo.getThumbnailUrl());
            playlist.setOwner(owner);
            playlist.setPublic(true);
            playlist.setCreatedAt(LocalDateTime.now());
            playlist.setUpdatedAt(LocalDateTime.now());
            
            // 4. PlaylistSong 엔티티들 생성
            for (YouTubeVideoInfo video : videos) {
                PlaylistSong song = new PlaylistSong();
                song.setPlaylist(playlist);
                song.setTitle(video.getTitle());
                song.setArtist(video.getChannelTitle());
                song.setYoutubeVideoId(video.getVideoId());
                song.setThumbnailUrl(video.getThumbnailUrl());
                song.setOrderIndex(video.getOrderIndex());
                
                playlist.getSongs().add(song);
            }
            
            // 5. DB에 저장
            return playlistRepository.save(playlist);
            
        } catch (Exception e) {
            // URL이 유효하지 않거나 API 호출 실패 시 null 반환
            return null;
        }
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

    /**
     * 사용자별 플레이리스트 목록을 조회합니다.
     * @param user 사용자
     * @return 플레이리스트 목록
     */
    public List<Playlist> getPlaylistsByUser(User user) {
        return playlistRepository.findByOwner(user);
    }

    /**
     * 전체 플레이리스트 목록을 조회합니다.
     * @return 전체 플레이리스트 목록
     */
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    /**
     * 플레이리스트 ID로 상세 정보를 조회합니다.
     * @param playlistId 플레이리스트 ID
     * @return 플레이리스트 정보
     */
    public Playlist getPlaylistById(Long playlistId) {
        Optional<Playlist> playlist = playlistRepository.findById(playlistId);
        return playlist.orElse(null);
    }

    /**
     * 플레이리스트를 삭제합니다.
     * @param playlistId 플레이리스트 ID
     * @param user 삭제 요청 사용자
     * @return 삭제 성공 여부
     */
    public boolean deletePlaylist(Long playlistId, User user) {
        Optional<Playlist> playlistOpt = playlistRepository.findById(playlistId);
        
        if (playlistOpt.isPresent()) {
            Playlist playlist = playlistOpt.get();
            
            // 소유자만 삭제 가능
            if (playlist.getOwner().equals(user)) {
                playlistRepository.delete(playlist);
                return true;
            }
        }
        
        return false;
    }
} 