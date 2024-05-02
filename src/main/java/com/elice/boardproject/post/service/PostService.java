package com.elice.boardproject.post.service;

import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import com.elice.boardproject.post.repository.PostRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(PostDTO postDTO) {
        return postRepository.save(postDTO.toEntity());
    }

    public List<PostDTO> findAllPostsbyBoardIdx(Long idx) {
        return postRepository.findAllPostsbyBoardIdx(idx);
    }

}
