package com.elice.boardproject.post.service;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final BoardService boardService;
    private CommentService commentService;

    public PostService(PostRepository postRepository, BoardService boardService, CommentService commentService){
        this.postRepository = postRepository;
        this.boardService = boardService;
        this.commentService = commentService;
    }

    public Page<Post> findPostsByBoardANDKeyword(Board board, String keyword, PageRequest pageRequest){
        if (keyword != null && !keyword.isEmpty()){
            return postRepository.findAllByBoardAndTitleContaining(board, keyword, pageRequest);
        }
        else {
            return postRepository.findAllByBoardOrderByPostDateDesc(board, pageRequest);
        }
    }
    public Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));
    }


    public List<Post> findPostsByBoardId(Long boardIdx){
        return postRepository.findPostsByBoardIdx(boardIdx);
    }

    public Post createPost(Post post, Long boardIdx){
        Board board = boardService.getBoardById(boardIdx);
        post.setBoard(board);
        Post createdPost = postRepository.save(post);
        return createdPost;
    }

    public Post updatePost(Post post){
        Long postId = post.getId();
        Post updatedPost = postRepository.findById(postId).orElse(null);
        updatedPost.setTitle(post.getTitle());
        updatedPost.setContent(post.getContent());

        return postRepository.save(updatedPost);
    }

    public void deletePost(Long postId){
        Post deletePost = postRepository.findById(postId).orElse(null);
        postRepository.delete(deletePost);
    }


}
