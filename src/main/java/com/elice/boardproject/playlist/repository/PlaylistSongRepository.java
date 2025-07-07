package com.elice.boardproject.playlist.repository;

import com.elice.boardproject.playlist.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
} 