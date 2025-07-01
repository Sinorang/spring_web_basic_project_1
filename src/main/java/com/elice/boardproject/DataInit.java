package com.elice.boardproject;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.entity.UserDTO;
import com.elice.boardproject.acc.repository.UserRepository;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.repository.BoardRepository;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.post.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    private UserService userService;

    public DataInit(UserRepository userRepository, BoardRepository boardRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 초기 데이터 삽입 - 비밀번호 "1234"를 해싱하여 저장
        UserDTO userDTO = new UserDTO();
        userDTO.setId("testid");
        userDTO.setPwd("qweasdzxc123!");  // 원본 비밀번호
        userDTO.setName("testuser");
        userDTO.setNickname("testNick");
        userDTO.setEmail("test@example.com");
        userService.join(userDTO);  // UserService에서 자동으로 해싱됨

        // 추가 테스트 사용자
        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId("admin");
        userDTO2.setPwd("1234");  // 원본 비밀번호
        userDTO2.setName("관리자");
        userDTO2.setNickname("admin");
        userDTO2.setEmail("admin@example.com");
        userService.join(userDTO2);  // UserService에서 자동으로 해싱됨

//        Board board1 = new Board("Board 1");
//        this.boardRepository.save(board1);
//
//        Post post1 = new Post("Post 1", "Content 1");
//        this.postRepository.save(post1);
//
//        Comment comment1 = new Comment("Comment 1");
//        this.commentRepository.save(comment1);
    }
}
