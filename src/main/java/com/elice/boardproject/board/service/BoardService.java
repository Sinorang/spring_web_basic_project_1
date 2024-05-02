package com.elice.boardproject.board.service;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.repository.BoardRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
    @Autowired
    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Board createBoard(BoardDTO boardDTO) {
        return boardRepository.save(boardDTO.toEntity());
    }

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Board getBoardById(Long boardIdx) {
        return boardRepository.findById(boardIdx).orElse(null);
    }

    public void deleteBoardById(Long boardIdx) {
        // 게시판 존재 여부 확인 후 삭제
        if (boardRepository.existsById(boardIdx)) {
            boardRepository.deleteById(boardIdx);
        } else {
            // 게시판이 존재하지 않을 경우 예외 처리 또는 다른 로직 수행
            throw new IllegalArgumentException("게시판이 존재하지 않습니다.");
        }
    }

    public Board updateBoard(Board board) {
        // BoardRepository의 save() 메서드 호출하여 수정된 게시글 저장
        return boardRepository.save(board);
    }
}
