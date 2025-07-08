package com.elice.boardproject.playlist.controller;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.playlist.dto.PlaylistCreateRequest;
import com.elice.boardproject.playlist.dto.PlaylistResponse;
import com.elice.boardproject.playlist.entity.Playlist;
import com.elice.boardproject.playlist.service.PlaylistService;
import com.elice.boardproject.security.JwtTokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 플레이리스트 생성 페이지
     */
    @GetMapping("/playlist/create")
    public String createPlaylistPage() {
        return "playlist/create";
    }

    /**
     * 전체 플레이리스트 목록 페이지
     */
    @GetMapping("/playlist/list")
    public String playlistListPage(Model model) {
        List<Playlist> allPlaylists = playlistService.getAllPlaylists();
        model.addAttribute("playlists", allPlaylists);
        return "playlist/list";
    }

    /**
     * 내 플레이리스트 목록 페이지
     */
    @GetMapping("/playlist/my")
    public String myPlaylistPage(Model model, HttpServletRequest request) {
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        if (currentUser != null) {
            List<Playlist> userPlaylists = playlistService.getPlaylistsByUser(currentUser);
            model.addAttribute("playlists", userPlaylists);
        }
        return "playlist/my";
    }

    /**
     * 플레이리스트 상세 페이지
     */
    @GetMapping("/playlist/{playlistId}")
    public String playlistDetailPage(@PathVariable Long playlistId, Model model, RedirectAttributes redirectAttributes) {
        Playlist playlist = playlistService.getPlaylistById(playlistId);
        
        if (playlist == null) {
            redirectAttributes.addFlashAttribute("message", "플레이리스트를 찾을 수 없습니다.");
            return "redirect:/playlist/list";
        }
        
        model.addAttribute("playlist", playlist);
        return "playlist/detail";
    }

    /**
     * 플레이리스트 생성 API
     */
    @PostMapping("/api/playlist/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPlaylist(@RequestBody PlaylistCreateRequest request, 
                                                             HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = jwtTokenUtil.getCurrentUser(httpRequest);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Playlist createdPlaylist = playlistService.createPlaylistFromYoutubeUrl(request.getYoutubeUrl(), currentUser);
            
            if (createdPlaylist != null) {
                response.put("success", true);
                response.put("message", "플레이리스트가 성공적으로 생성되었습니다.");
                response.put("playlistId", createdPlaylist.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "유효하지 않은 YouTube 플레이리스트 URL입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "플레이리스트 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 플레이리스트 삭제 API
     */
    @DeleteMapping("/api/playlist/{playlistId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePlaylist(@PathVariable Long playlistId, 
                                                             HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = jwtTokenUtil.getCurrentUser(request);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean deleted = playlistService.deletePlaylist(playlistId, currentUser);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "플레이리스트가 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "플레이리스트를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "플레이리스트 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 