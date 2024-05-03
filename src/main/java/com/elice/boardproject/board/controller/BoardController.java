package com.elice.boardproject.board.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.service.PostService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    @Autowired
    public BoardController(BoardService boardService,
                           PostService postService,
                           UserService userService,
                           CommentService commentService){
        this.boardService = boardService;
        this.postService = postService;
        this.userService = userService;
        this.commentService= commentService;
    }

    @RequestMapping("/board/boards")
    public String getAllBoards(Model model) {
        List<Board> boards = boardService.getAllBoards();
        model.addAttribute("boards", boards);
        return "board/boards";
    }

    @GetMapping("/board/index/{boardIdx}")
    public String getBoardPage(@PathVariable Long boardIdx,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        Board board = boardService.getBoardById(boardIdx);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Post> postPage = postService.findPostsByBoardANDKeyword(board, keyword, pageRequest);

        model.addAttribute("board", board);
        model.addAttribute("keyword", keyword);
        model.addAttribute("postPage", postPage);
        return "board/index";
    }

    @GetMapping("/board/create")
    public String createBoardPage(HttpSession session) {
        return "board/createBoard";
    }

    @PostMapping("/board/create")
    public String createBoard(BoardDTO boardDTO, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        boardDTO.setUser(loginUser);
        boardService.createBoard(boardDTO);
        return "redirect:/board/boards";
    }

    @GetMapping("/board/boards/{boardIdx}/edit")
    public String editBoardPage(@PathVariable("boardIdx") Long boardIdx, Model model
                                , HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        Board board = boardService.getBoardById(boardIdx);

        if(board.getUser().getIdx().equals(loginUser.getIdx())) {
            model.addAttribute("board", board);
            return "/board/editBoard";
        }
        else {
            System.out.println("게시판을 생성한 사람만 수정할 수 있습니다!");
            return "redirect:/board/boards";
        }
    }

    @PostMapping("/board/boards/{boardIdx}/edit")
    public String updateBoard(@PathVariable Long boardIdx, @ModelAttribute Board board) {
        // 엔티티 ID를 설정하여 해당 ID에 해당하는 엔티티를 수정합니다.
        Board editBoard = boardService.getBoardById(boardIdx);
        editBoard.setName(board.getName());
        editBoard.setDescription(board.getDescription());
        boardService.updateBoard(editBoard);
        return "redirect:/board/boards";
    }

    @GetMapping("/board/boards/{boardIdx}/delete")
    public String deleteBoard(@RequestParam("boardIdx") Long boardIdx, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        Board board = boardService.getBoardById(boardIdx);

        if(board.getUser().getIdx().equals(loginUser.getIdx())) {
            List<Post> postList = postService.findPostsByBoardId(boardIdx);
            for(int i = 0; i<postList.size(); i++) {
                Post post = postList.get(i);

                List<Comment> commentList = commentService.findCommentByPostId(post.getId());
                for(int j = 0; j<commentList.size(); j++) {
                    Comment comment = commentList.get(j);
                    commentService.deleteComment(comment.getCommentId());
                }
                postService.deletePost(post.getId());
            }

            boardService.deleteBoardById(boardIdx);
            return "redirect:/board/boards";
        }
        else{
            System.out.println("게시판을 생성한 사람만 삭제할 수 있습니다!");
            return "redirect:/board/baords";
        }
    }
}
