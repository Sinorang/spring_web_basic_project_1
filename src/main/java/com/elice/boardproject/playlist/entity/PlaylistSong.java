package com.elice.boardproject.playlist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSong {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Playlist playlist;

    private String title;
    private String artist;
    private String album;
    private String youtubeVideoId;
    private String thumbnailUrl;
    private int orderIndex;

    // getter/setter, 생성자 등은 TDD 과정에서 필요에 따라 추가
} 