package com.elice.boardproject.playlist.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {
    private Long id;
    private String title;
    private String description;
    private String youtubePlaylistId;
    private String coverImageUrl;
    private String ownerName;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private List<PlaylistSongResponse> songs;
} 