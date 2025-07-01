package com.elice.boardproject.comment.controller;

import com.elice.boardproject.JwtTokenUtil;
import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.acc.service.UserService;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.entity.CommentDTO;
import com.elice.boardproject.comment.mapper.CommentMapper;
import com.elice.boardproject.comment.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/comment") // 댓글 작성 요청
    public String createComment(@ModelAttribute CommentDTO commentDTO,
                                @RequestParam Long postId,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {

        Comment comment = commentMapper.commentDtoToComment(commentDTO);
        User loginUser = jwtTokenUtil.getCurrentUser(request);
        comment.setUser(loginUser);
        commentService.createComment(postId, comment);
        System.out.println(commentDTO.getCommentContent());
        redirectAttributes.addAttribute("post_id", postId);
        return "redirect:/post/{post_id}"; // 해당 게시글 화면으로 다시 돌아감
    }

    @PostMapping("/comment/{comment_id}/edit") // 댓글 수정 요청
    public String updateComment(@PathVariable Long comment_id,
                                @ModelAttribute CommentDTO commentDTO,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {

        Comment basecomment = commentService.findCommentByCommentId(comment_id);
        User loginUser = jwtTokenUtil.getCurrentUser(request);

        if (basecomment.getUser().getId().equals(loginUser.getId())) {
            Comment comment = commentMapper.commentDtoToComment(commentDTO);
            Comment updateComment = commentService.updateComment(comment_id, comment);
            redirectAttributes.addAttribute("postId", updateComment.getPost().getId());
            return "redirect:/post/{postId}"; // 해당 게시글 화면으로 다시 돌아감
        } else {
            System.out.println("댓글을 작성한 사람만 수정할 수 있습니다!");
            redirectAttributes.addAttribute("postId", basecomment.getPost().getId());
            return "redirect:/post/{postId}";
        }
    }

    @DeleteMapping("/comment/{comment_id}/delete") //댓글 삭제 요청
    public String deleteComment(@PathVariable Long comment_id,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest request) {

        User loginUser = jwtTokenUtil.getCurrentUser(request);
        Comment comment = commentService.findCommentByCommentId(comment_id);
        System.out.println(comment.getUser().getId());

        if(comment.getUser().getId().equals(loginUser.getId())){
            Long postId = comment.getPost().getId();
            commentService.deleteComment(comment_id);
            return "/post/" + postId; // 해당 게시글 화면으로 다시 돌아감

        } else {
            System.out.println("댓글을 작성한 사람만 삭제할 수 있습니다!");
            redirectAttributes.addAttribute("postId", comment.getPost().getId());
            return "/post/{postId}";
        }
    }


}