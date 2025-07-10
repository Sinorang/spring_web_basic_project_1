package com.elice.boardproject.admin.controller;

import com.elice.boardproject.admin.service.AdminUserManagementService;
import com.elice.boardproject.playlist.service.PlaylistService;
import com.elice.boardproject.post.service.PostService;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminUserManagementService adminUserManagementService;
    private final PlaylistService playlistService;
    private final PostService postService;
    private final CommentService commentService;
    private final BoardService boardService;

    /**
     * 관리자 대시보드
     */
    @GetMapping("")
    public String dashboard(Model model) {
        log.info("관리자 대시보드 접근");
        
        // 통계 데이터 조회
        long totalUsers = adminUserManagementService.getUserStatistics().getTotalUsers();
        long totalPlaylists = playlistService.countAllPlaylists();
        long totalPosts = postService.getPostCount();
        long totalComments = commentService.getCommentCount();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPlaylists", totalPlaylists);
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalComments", totalComments);
        
        return "admin/dashboard";
    }

    /**
     * 플레이리스트 관리 페이지
     */
    @GetMapping("/playlists")
    public String playlistManagement(Model model) {
        log.info("관리자 플레이리스트 관리 페이지 접근");
        
        // 전체 플레이리스트 목록 조회
        var playlists = playlistService.getAllPlaylists();
        model.addAttribute("playlists", playlists);
        
        return "admin/playlist-management";
    }

    /**
     * 게시글 관리 페이지
     */
    @GetMapping("/posts")
    public String postManagement(@RequestParam(value = "boardId", required = false) Long boardId, Model model) {
        log.info("관리자 게시글 관리 페이지 접근");
        var boards = boardService.getAllBoards();
        var posts = (boardId != null) ? postService.findPostsByBoardId(boardId) : postService.getAllPosts();
        model.addAttribute("boards", boards);
        model.addAttribute("posts", posts);
        model.addAttribute("selectedBoardId", boardId);
        return "admin/post-management";
    }

    /**
     * 댓글 관리 페이지
     */
    @GetMapping("/comments")
    public String commentManagement(@RequestParam(value = "boardId", required = false) Long boardId, Model model) {
        log.info("관리자 댓글 관리 페이지 접근");
        var boards = boardService.getAllBoards();
        var comments = (boardId != null) ? commentService.findCommentsByBoardId(boardId) : commentService.getAllComments();
        model.addAttribute("boards", boards);
        model.addAttribute("comments", comments);
        model.addAttribute("selectedBoardId", boardId);
        return "admin/comment-management";
    }

    /**
     * 사용자 관리 페이지
     */
    @GetMapping("/users")
    public String userManagement(Model model) {
        log.info("관리자 사용자 관리 페이지 접근");
        return "admin/user-management";
    }
} 