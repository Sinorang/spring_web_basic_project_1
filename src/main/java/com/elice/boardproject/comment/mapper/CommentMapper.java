package com.elice.boardproject.comment.mapper;

import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.entity.CommentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class CommentMapper {
    public Comment commentDtoToComment(CommentDTO commentDTO) {
        if (commentDTO == null) {
            return null;
        } else {
            Comment comment = new Comment();
            comment.setCommentContent(commentDTO.getCommentContent());
            return comment;
        }
    }
}

