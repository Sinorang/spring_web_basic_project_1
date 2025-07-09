package com.elice.boardproject.board.controller;

import com.elice.boardproject.aop.annotation.RequirePermission;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.exception.ErrorCode;
import com.elice.boardproject.exception.ExceptionUtils;
import com.elice.boardproject.security.JwtTokenUtil;
import com.elice.boardproject.acc.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 게시판 권한별 REST API 컨트롤러
 * 각 API는 세분화된 권한 검증을 통해 접근 제어
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardApiController {

    private final BoardService boardService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 게시판 목록 조회 (BOARD_READ 권한 필요)
     */
    @GetMapping
    @RequirePermission("BOARD_READ")
    public ResponseEntity<Map<String, Object>> getAllBoards(HttpServletRequest request) {
        try {
            List<Board> boards = boardService.getAllBoards();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", boards,
                "message", "게시판 목록을 성공적으로 조회했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "게시판 목록 조회 중 오류가 발생했습니다.",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * 특정 게시판 조회 (BOARD_READ 권한 필요)
     */
    @GetMapping("/{boardIdx}")
    @RequirePermission("BOARD_READ")
    public ResponseEntity<Map<String, Object>> getBoard(@PathVariable Long boardIdx, 
                                                       HttpServletRequest request) {
        try {
            Board board = boardService.getBoardById(boardIdx);
            if (board == null) {
                ExceptionUtils.throwException(ErrorCode.BOARD_NOT_FOUND, boardIdx);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", board,
                "message", "게시판을 성공적으로 조회했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "error", "게시판을 찾을 수 없습니다.",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * 게시판 생성 (BOARD_CREATE 권한 필요)
     */
    @PostMapping
    @RequirePermission("BOARD_CREATE")
    public ResponseEntity<Map<String, Object>> createBoard(@RequestBody BoardDTO boardDTO, 
                                                          HttpServletRequest request) {
        try {
            User loginUser = jwtTokenUtil.getCurrentUser(request);
            if (loginUser == null) {
                ExceptionUtils.throwException(ErrorCode.UNAUTHORIZED);
            }
            
            boardDTO.setUser(loginUser);
            Board createdBoard = boardService.createBoard(boardDTO);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "data", createdBoard,
                    "message", "게시판이 성공적으로 생성되었습니다."
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "게시판 생성에 실패했습니다.",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * 게시판 수정 (BOARD_UPDATE 권한 필요)
     */
    @PutMapping("/{boardIdx}")
    @RequirePermission("BOARD_UPDATE")
    public ResponseEntity<Map<String, Object>> updateBoard(@PathVariable Long boardIdx, 
                                                          @RequestBody BoardDTO boardDTO,
                                                          HttpServletRequest request) {
        try {
            User loginUser = jwtTokenUtil.getCurrentUser(request);
            if (loginUser == null) {
                ExceptionUtils.throwException(ErrorCode.UNAUTHORIZED);
            }
            
            Board existingBoard = boardService.getBoardById(boardIdx);
            if (existingBoard == null) {
                ExceptionUtils.throwException(ErrorCode.BOARD_NOT_FOUND, boardIdx);
            }
            
            // 게시판 작성자 또는 관리자만 수정 가능
            if (!existingBoard.getUser().getIdx().equals(loginUser.getIdx())) {
                ExceptionUtils.throwException(ErrorCode.BOARD_UPDATE_DENIED);
            }
            
            existingBoard.setName(boardDTO.getName());
            existingBoard.setDescription(boardDTO.getDescription());
            Board updatedBoard = boardService.updateBoard(existingBoard);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", updatedBoard,
                "message", "게시판이 성공적으로 수정되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "게시판 수정에 실패했습니다.",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * 게시판 삭제 (BOARD_DELETE 권한 필요)
     */
    @DeleteMapping("/{boardIdx}")
    @RequirePermission("BOARD_DELETE")
    public ResponseEntity<Map<String, Object>> deleteBoard(@PathVariable Long boardIdx, 
                                                          HttpServletRequest request) {
        try {
            User loginUser = jwtTokenUtil.getCurrentUser(request);
            if (loginUser == null) {
                ExceptionUtils.throwException(ErrorCode.UNAUTHORIZED);
            }
            
            Board board = boardService.getBoardById(boardIdx);
            if (board == null) {
                ExceptionUtils.throwException(ErrorCode.BOARD_NOT_FOUND, boardIdx);
            }
            
            // 게시판 작성자 또는 관리자만 삭제 가능
            if (!board.getUser().getIdx().equals(loginUser.getIdx())) {
                ExceptionUtils.throwException(ErrorCode.BOARD_DELETE_DENIED);
            }
            
            boardService.deleteBoardById(boardIdx);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "게시판이 성공적으로 삭제되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "게시판 삭제에 실패했습니다.",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * 게시판 통계 조회 (BOARD_READ 권한 필요)
     */
    @GetMapping("/{boardIdx}/statistics")
    @RequirePermission("BOARD_READ")
    public ResponseEntity<Map<String, Object>> getBoardStatistics(@PathVariable Long boardIdx, 
                                                                 HttpServletRequest request) {
        try {
            Board board = boardService.getBoardById(boardIdx);
            if (board == null) {
                ExceptionUtils.throwException(ErrorCode.BOARD_NOT_FOUND, boardIdx);
            }
            
            // 게시판 통계 정보 - 안전한 posts 접근
            Map<String, Object> statistics = Map.of(
                "boardIdx", board.getIdx(),
                "boardName", board.getName(),
                "postCount", Optional.ofNullable(board.getPosts()).map(List::size).orElse(0),
                "createdDate", board.getDate(),
                "creator", Optional.ofNullable(board.getUser()).map(User::getNickname).orElse("Unknown")
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", statistics,
                "message", "게시판 통계를 성공적으로 조회했습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", "게시판 통계 조회에 실패했습니다.",
                    "message", e.getMessage()
                ));
        }
    }
} 