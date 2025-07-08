package com.elice.boardproject.playlist.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistCreateRequest {
    private String youtubeUrl;
} 