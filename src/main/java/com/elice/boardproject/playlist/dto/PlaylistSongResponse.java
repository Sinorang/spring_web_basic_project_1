package com.elice.boardproject.playlist.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongResponse {
    private Long id;
    private String title;
    private String artist;
    private String youtubeVideoId;
    private String thumbnailUrl;
    private int orderIndex;
} 