package com.elice.boardproject.admin.controller;

import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.service.PostService;
import com.elice.boardproject.playlist.service.PlaylistService;
import com.elice.boardproject.playlist.entity.Playlist;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 관리자 전용 컨텐츠 관리 API
 * 게시글과 댓글의 관리자 권한 삭제 기능 제공
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {

    private final PostService postService;
    private final CommentService commentService;
    private final PlaylistService playlistService;

    /**
     * 관리자용 게시글 삭제 API
     * 관리자는 모든 게시글을 삭제할 수 있습니다.
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Map<String, Object>> deletePostAsAdmin(@PathVariable Long postId) {
        try {
            // 게시글 존재 여부 확인
            var post = postService.findPost(postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            // 관리자 권한으로 게시글 삭제
            postService.deletePost(postId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관리자 권한으로 게시글이 삭제되었습니다.",
                "deletedPostId", postId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "게시글 삭제 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 관리자용 댓글 삭제 API
     * 관리자는 모든 댓글을 삭제할 수 있습니다.
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteCommentAsAdmin(@PathVariable Long commentId) {
        try {
            // 댓글 존재 여부 확인
            var comment = commentService.findCommentByCommentId(commentId);
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }

            // 관리자 권한으로 댓글 삭제
            commentService.deleteComment(commentId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관리자 권한으로 댓글이 삭제되었습니다.",
                "deletedCommentId", commentId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "댓글 삭제 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 관리자용 플레이리스트 삭제 API
     * 관리자는 모든 플레이리스트를 삭제할 수 있습니다.
     */
    @DeleteMapping("/playlists/{playlistId}")
    public ResponseEntity<Map<String, Object>> deletePlaylistAsAdmin(@PathVariable Long playlistId) {
        try {
            playlistService.getPlaylistById(playlistId);
            playlistService.deletePlaylistAsAdmin(playlistId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "관리자 권한으로 플레이리스트가 삭제되었습니다.",
                "deletedPlaylistId", playlistId
            ));
        } catch (com.elice.boardproject.exception.PliException e) {
            if (e.getErrorCode() == com.elice.boardproject.exception.ErrorCode.PLAYLIST_NOT_FOUND) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "플레이리스트 삭제 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "플레이리스트 삭제 중 오류가 발생했습니다.",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 관리자용 플레이리스트 통계 API
     */
    @GetMapping("/playlists/statistics")
    public ResponseEntity<Map<String, Object>> getPlaylistStatistics() {
        long totalPlaylists = playlistService.countAllPlaylists();
        return ResponseEntity.ok(Map.of(
            "totalPlaylists", totalPlaylists
        ));
    }
} 