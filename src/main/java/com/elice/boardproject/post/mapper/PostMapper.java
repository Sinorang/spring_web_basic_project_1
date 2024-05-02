package com.elice.boardproject.post.mapper;

import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class PostMapper {
    public Post postDTOToPost(PostDTO postDTO) {
        if (postDTO == null) {
            return null;
        } else {
            Post post = new Post();
            post.setTitle(postDTO.getTitle());
            post.setContent(postDTO.getContent());
            return post;
        }
    }
}
