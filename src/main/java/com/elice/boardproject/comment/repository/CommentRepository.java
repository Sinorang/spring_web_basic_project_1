package com.elice.boardproject.comment.repository;

import com.elice.boardproject.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
