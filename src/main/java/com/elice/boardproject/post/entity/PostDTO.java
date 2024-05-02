package com.elice.boardproject.post.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostDTO {
    private String title;
    private String content;
}