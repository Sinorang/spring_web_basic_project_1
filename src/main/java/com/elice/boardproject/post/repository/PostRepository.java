package com.elice.boardproject.post.repository;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.post.entity.Post;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByBoardOrderByPostDateDesc(Board board, Pageable pageable);

    Page<Post> findAllByBoardAndTitleContaining(Board board, String keyword, Pageable pageable);

    Object findPostById(Long postId);

    List<Post> findPostsByBoardIdx(Long boardIdx);
    
    // Board와 User를 함께 조회하는 메서드
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p JOIN FETCH p.board JOIN FETCH p.user WHERE p.id = :postId")
    Post findPostWithBoardAndUser(@org.springframework.data.repository.query.Param("postId") Long postId);
    
    // Post ID로 Board ID를 조회하는 메서드
    @org.springframework.data.jpa.repository.Query("SELECT p.board.idx FROM Post p WHERE p.id = :postId")
    Long findBoardIdxByPostId(@org.springframework.data.repository.query.Param("postId") Long postId);
}
