package com.elice.boardproject.board.controller;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
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
public class BoardController {

    private final BoardService boardService;
    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @RequestMapping("/board/boards")
    public String getAllBoards(Model model) {
        List<Board> boards = boardService.getAllBoards();
        model.addAttribute("boards", boards);
        return "board/boards";
    }

    @GetMapping("/board/index/{boardIdx}")
    public String getBoardPage(@PathVariable("boardIdx") Long boardIdx, Model model) {
        Board board = boardService.getBoardById(boardIdx);

        // 모델에 게시글 정보를 추가합니다.
        model.addAttribute("board", board);

        return "board/index";
    }

    @RequestMapping("/board/create")
    public String createBoardPage() {
        return "board/createBoard";
    }

    @PostMapping("/board/create")
    public String createBoard(BoardDTO boardDTO) {
        boardService.createBoard(boardDTO);
        return "redirect:/board/boards";
    }

    @GetMapping("/board/boards/{boardIdx}/delete")
    public String deleteBoard(@RequestParam("boardIdx") Long boardIdx) {
        boardService.deleteBoardById(boardIdx);
        return "redirect:/board/boards";
    }

    @GetMapping("/board/boards/{boardIdx}/edit")
    public String editBoardPage(@PathVariable("boardIdx") Long boardIdx, Model model) {
        Board board = boardService.getBoardById(boardIdx);

        // 모델에 게시글 정보를 추가합니다.
        model.addAttribute("board", board);

        return "board/editBoard";
    }

    @PostMapping("/board/boards/{boardIdx}/edit")
    public String updateBoard(@PathVariable Long boardIdx, @ModelAttribute("board") Board board) {
        // 엔티티 ID를 설정하여 해당 ID에 해당하는 엔티티를 수정합니다.
        board.setIdx(boardIdx);
        boardService.updateBoard(board);
        return "redirect:/board/boards";
    }
}
