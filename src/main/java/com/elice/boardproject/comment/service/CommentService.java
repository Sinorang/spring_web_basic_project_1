package com.elice.boardproject.comment.service;

import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.repository.PostRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository){
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public List<Comment> findComments() { return commentRepository.findAll(); } //댓글 전체 조회
    public List<Comment> findCommentByPostId(Long postId) { // 포스트 아이디로 댓글 조회
        return commentRepository.findCommentsByPostId(postId);
    }
    public Comment findCommentByCommentId(Long commentId){ // 댓글아이디로 댓글 조회
        return commentRepository.findById(commentId).orElse(null);
    }

    public Comment createComment(Long postId, Comment comment){ // 댓글 생성
        Post post = postRepository.findById(postId).orElse(null);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, Comment comment){ //댓글 수정
        Comment updateComment = commentRepository.findCommentByCommentId(commentId);
        updateComment.setCommentContent(comment.getCommentContent());
        return commentRepository.save(updateComment);
    }

    public void deleteComment(Long commentId){ // 댓글삭제
        Comment deleteComment = commentRepository.findById(commentId).orElse(null);
        commentRepository.delete(deleteComment);
    }

}