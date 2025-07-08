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
import com.elice.boardproject.exception.PliException;
import com.elice.boardproject.exception.ErrorCode;

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
    public String playlistListPage(Model model, HttpServletRequest request) {
        List<Playlist> allPlaylists = playlistService.getAllPlaylists();
        model.addAttribute("playlists", allPlaylists);
        
        // 현재 사용자 정보 추가
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        model.addAttribute("currentUser", currentUser);
        
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
            model.addAttribute("currentUser", currentUser);
        }
        return "playlist/my";
    }

    /**
     * 플레이리스트 상세 페이지
     */
    @GetMapping("/playlist/{playlistId}")
    public String playlistDetailPage(@PathVariable Long playlistId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Playlist playlist = playlistService.getPlaylistById(playlistId);
            model.addAttribute("playlist", playlist);
            return "playlist/detail";
        } catch (com.elice.boardproject.exception.PliException e) {
            redirectAttributes.addFlashAttribute("message", "존재하지 않는 페이지입니다.");
            return "redirect:/playlist/list";
        }
    }

    /**
     * 플레이리스트 생성 API
     */
    @PostMapping("/api/playlist/create")
    @ResponseBody
    public ResponseEntity<?> createPlaylist(@RequestBody PlaylistCreateRequest request, 
                                            HttpServletRequest httpRequest) {
        User currentUser = jwtTokenUtil.getCurrentUser(httpRequest);
        if (currentUser == null) {
            throw new PliException(ErrorCode.UNAUTHORIZED);
        }
        Playlist createdPlaylist = playlistService.createPlaylistFromYoutubeUrl(request.getYoutubeUrl(), currentUser);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "플레이리스트가 성공적으로 생성되었습니다.");
        response.put("playlistId", createdPlaylist.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 플레이리스트 삭제 API
     */
    @DeleteMapping("/api/playlist/{playlistId}")
    @ResponseBody
    public ResponseEntity<?> deletePlaylist(@PathVariable Long playlistId, 
                                            HttpServletRequest request) {
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        if (currentUser == null) {
            throw new PliException(ErrorCode.UNAUTHORIZED);
        }
        playlistService.deletePlaylist(playlistId, currentUser);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "플레이리스트가 성공적으로 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }
} 