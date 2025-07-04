package com.elice.boardproject.post.controller;

import com.elice.boardproject.security.JwtTokenUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import com.elice.boardproject.board.service.BoardService;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.entity.PostDTO;
import com.elice.boardproject.post.mapper.PostMapper;
import com.elice.boardproject.post.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.AllArgsConstructor;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final PostMapper postMapper;
    private final CommentService commentService;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/post/{post_id}") // 게시글 눌렀을 때 화면 : 게시글 조회
    public String postMain(@PathVariable Long post_id, Model model,
                           HttpServletRequest request){

        Post post = postService.findPost(post_id);
        
        if (post == null) {
            return "redirect:/board/boards";
        }
        
        model.addAttribute("post", post);



        // Board 정보를 별도로 모델에 추가 (명확하게)
        Long boardIdx = null;
        if (post.getBoard() != null) {
            boardIdx = post.getBoard().getIdx();
        } else {
            boardIdx = postService.getBoardIdxByPostId(post_id);
        }
        model.addAttribute("boardIdx", boardIdx);
        //코멘트 추가
        List<Comment> commentList = commentService.findCommentByPostId(post_id);
        model.addAttribute("comments", commentList);
        return "post/post";
    }

    @GetMapping("/post/create") //게시글 생성창
    public String createPostPage(@RequestParam Long boardIdx, Model model) {

        model.addAttribute("boardIdx", boardIdx);
        return "post/createPost";
    }

    @PostMapping("/post/create") // 게시글 생성 요청
    public String createPost(@ModelAttribute PostDTO postDTO,
                             @RequestParam Long boardIdx,
                             HttpServletRequest request) {

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Post post = postMapper.postDTOToPost(postDTO);
        post.setUser(loginUser);
        Post createPost = postService.createPost(post, boardIdx);
        return "redirect:/board/index/" + createPost.getBoard().getIdx(); // 생성한 게시글 조회 화면으로 이동
    }

    @GetMapping("/post/{post_id}/edit") // 게시글 수정창
    public String editPostPage(@PathVariable Long post_id, Model model,
                               HttpServletRequest request) {

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Post post = postService.findPost(post_id);
        
        if (post == null) {
            return "redirect:/board/boards";
        }
        
        if (post.getUser().getIdx().equals(loginUser.getIdx())) {
            model.addAttribute("post", post);
            return "post/editPost";
        } else {
            System.out.println("게시글을 작성한 사람만 수정할 수 있습니다!");
            return "redirect:/post/" + post_id;
        }
    }

    @PostMapping("/post/{post_id}/edit") // 수정 요청
    public String editPost(@PathVariable Long post_id,
                           @ModelAttribute PostDTO postDTO,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Post existingPost = postService.findPost(post_id);
        
        if (existingPost == null) {
            redirectAttributes.addFlashAttribute("message", "게시글을 찾을 수 없습니다!");
            return "redirect:/board/boards";
        }
        
        if (!existingPost.getUser().getIdx().equals(loginUser.getIdx())) {
            redirectAttributes.addFlashAttribute("message", "게시글을 작성한 사람만 수정할 수 있습니다!");
            return "redirect:/post/" + post_id;
        }

        Post post = postMapper.postDTOToPost(postDTO);
        post.setId(post_id);
        Post editPost = postService.updatePost(post);

        redirectAttributes.addAttribute("post_id", editPost.getId());
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/post/{post_id}"; // 수정한 게시글 조회 화면
    }

    @DeleteMapping("post/{post_id}/delete")
    public String deletePost(@PathVariable Long post_id,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Post post = postService.findPost(post_id);
        
        if (post == null) {
            redirectAttributes.addFlashAttribute("message", "게시글을 찾을 수 없습니다!");
            return "redirect:/board/boards";
        }
        

        
        // Board ID를 안전하게 가져오기
        Long boardIdx = postService.getBoardIdxByPostId(post_id);
        
        if (post.getUser().getIdx().equals(loginUser.getIdx())) {
            List<Comment> commentList = commentService.findCommentByPostId(post_id);
            for (int i = 0; i < commentList.size(); i++) {
                Comment comment = commentList.get(i);
                commentService.deleteComment(comment.getCommentId());
            }
            postService.deletePost(post_id);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다!");
            
            if (boardIdx != null) {
                return "redirect:/board/index/" + boardIdx; // 해당 게시판으로 리다이렉트
            } else {
                return "redirect:/board/boards"; // 게시판 목록으로 리다이렉트
            }
        }
        else {
            System.out.println("게시글을 생성한 사람만 삭제할 수 있습니다!");
            redirectAttributes.addFlashAttribute("message", "게시글을 생성한 사람만 삭제할 수 있습니다!");
            return "redirect:/post/" + post_id + "?loginId=" + loginUser.getId() + "&loginNickname=" + loginUser.getNickname();
        }
    }
}