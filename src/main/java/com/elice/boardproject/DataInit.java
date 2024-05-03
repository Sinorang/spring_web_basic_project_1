package com.elice.boardproject;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.repository.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public DataInit(UserRepository userRepository, BoardRepository boardRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) {
        // 초기 데이터 삽입
        User user = new User("test", "1234", "testUser", "testNick", "test@example.com");
        this.userRepository.save(user);

//        Board board1 = new Board("Board 1");
//        Board board2 = new Board("Board 2");
//        boardRepository.save(board1);
//        boardRepository.save(board2);
//
//        Post post1 = new Post("Title 1", "Content 1");
//        post1.setBoard(board1);
//        postRepository.save(post1);
//
//        Comment comment1 = new Comment("Comment 1");
//        comment1.setPost(post1);
//        commentRepository.save(comment1);
    }
}
