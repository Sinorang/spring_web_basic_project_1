package com.elice.boardproject.post.controller;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import com.elice.boardproject.post.service.PostService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/post/posts")
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/board/index/{boardIdx}")
    public String getAllPostsbyBoardIdx(@PathVariable(name = "boardIdx") Long idx, Model model) {
        List<PostDTO> posts = postService.findAllPostsbyBoardIdx(idx);
        model.addAttribute("posts", posts);
        return "board/index";
    }

    @GetMapping("/post/create")
    public String createPostPage() {
        return "post/createPost";
    }

    @PostMapping("/post/create")
    public String createPost(PostDTO postDTO) {
        postService.createPost(postDTO);
        return "redirect:/board/index";
    }
//
//    @GetMapping("/board/boards/{boardIdx}/delete")
//    public String deleteBoard(@RequestParam("boardIdx") Long boardIdx) {
//        boardService.deleteBoardById(boardIdx);
//        return "redirect:/board/boards";
//    }
//
//    @GetMapping("/board/boards/{boardIdx}/edit")
//    public String editBoardPage(@PathVariable("boardIdx") Long boardIdx, Model model) {
//        Board board = boardService.getBoardById(boardIdx);
//
//        // 모델에 게시글 정보를 추가합니다.
//        model.addAttribute("board", board);
//
//        return "board/editBoard";
//    }
//
//    @PostMapping("/board/boards/{boardIdx}/edit")
//    public String updateBoard(@PathVariable Long boardIdx, @ModelAttribute("board") Board board) {
//        // 엔티티 ID를 설정하여 해당 ID에 해당하는 엔티티를 수정합니다.
//        board.setIdx(boardIdx);
//        boardService.updateBoard(board);
//        return "redirect:/board/boards";
//    }
}
