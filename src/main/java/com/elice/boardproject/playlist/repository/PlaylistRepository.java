package com.elice.boardproject.playlist.repository;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.playlist.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    /**
     * 사용자별 플레이리스트 목록을 조회합니다.
     * @param owner 플레이리스트 소유자
     * @return 플레이리스트 목록
     */
    List<Playlist> findByOwner(User owner);
} 