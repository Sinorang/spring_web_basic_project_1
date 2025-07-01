package com.elice.boardproject.post.controller;

import com.elice.boardproject.JwtTokenUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
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

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Post post = postService.findPost(post_id);
        model.addAttribute("post", post);
        if (loginUser != null) {
            model.addAttribute("loginId", loginUser.getId());
        }
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
        if (post.getUser().getId().equals(loginUser.getId())) {
            model.addAttribute("post", post);

            return "post/editPost";
        } else {
            System.out.println("게시글을 작성한 사람만 수정할 수 있습니다!");
            return "redirect:/post/{post_id}";
        }
    }

    @PostMapping("/post/{post_id}/edit") // 수정 요청
    public String editPost(@PathVariable Long post_id,
                           @ModelAttribute PostDTO postDTO,
                           RedirectAttributes redirectAttributes) {

        Post post = postMapper.postDTOToPost(postDTO);
        post.setId(post_id);
        System.out.println(post.getTitle());
        System.out.println(post.getContent());
        System.out.println(post.getId());
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
        if (post.getUser().getId().equals(loginUser.getId())) {
            List<Comment> commentList = commentService.findCommentByPostId(post_id);
            for (int i = 0; i < commentList.size(); i++) {
                Comment comment = commentList.get(i);
                commentService.deleteComment(comment.getCommentId());
            }
            postService.deletePost(post_id);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다!");
            return "redirect:/board/index"; //게시판 메인화면
        }
        else {
            System.out.println("게시글을 생성한 사람만 삭제할 수 있습니다!");
            return "redirect:/post/{post_id}";
        }
    }
}