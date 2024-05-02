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
}
