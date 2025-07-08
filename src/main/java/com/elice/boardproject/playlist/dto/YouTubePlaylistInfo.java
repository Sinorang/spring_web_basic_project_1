package com.elice.boardproject.playlist.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class YouTubePlaylistInfo {
    private String playlistId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String channelTitle;
    private String publishedAt;
    private int videoCount;
} 