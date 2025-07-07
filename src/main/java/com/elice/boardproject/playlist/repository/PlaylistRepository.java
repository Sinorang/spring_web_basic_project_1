package com.elice.boardproject.playlist.repository;

import com.elice.boardproject.playlist.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
} 