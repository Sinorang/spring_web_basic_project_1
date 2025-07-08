package com.elice.boardproject.playlist.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideoInfo {
    private String videoId;
    private String title;
    private String description;
    private String channelTitle;
    private String thumbnailUrl;
    private String publishedAt;
    private int orderIndex;
    private String duration;
} 