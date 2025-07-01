package com.elice.boardproject.comment.mapper;

import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.entity.CommentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    // DTO → Entity
    Comment commentDtoToComment(CommentDTO commentDTO);

    // Entity → DTO (필요시)
    CommentDTO commentToCommentDto(Comment comment);
}

