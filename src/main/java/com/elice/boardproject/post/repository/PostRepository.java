package com.elice.boardproject.post.repository;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByBoardOrderByCreatedAtDesc(Board board, Pageable pageable);

    Page<Post> findAllByBoardAndPostTitleContaining(Board board, String keyword, Pageable pageable);

    Object findByPostId(int postId);

    List<Post> findPostsByBoardBoardId(int boardId);
}
