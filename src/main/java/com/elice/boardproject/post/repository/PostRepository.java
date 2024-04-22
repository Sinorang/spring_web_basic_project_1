package com.elice.boardproject.post.repository;

import com.elice.boardproject.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
