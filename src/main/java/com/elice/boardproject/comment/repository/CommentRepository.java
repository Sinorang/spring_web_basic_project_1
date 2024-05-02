package com.elice.boardproject.comment.repository;

import com.elice.boardproject.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findCommentsByPostPostId(int postId);
    Comment findCommentByCommentId(int commentId);
}