package com.elice.boardproject.comment;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.comment.entity.Comment;
import com.elice.boardproject.comment.repository.CommentRepository;
import com.elice.boardproject.comment.service.CommentService;
import com.elice.boardproject.post.entity.Post;
import com.elice.boardproject.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId("testuser");
        post = new Post();
        post.setId(1L);
        comment = new Comment();
        comment.setCommentContent("테스트 댓글");
        comment.setUser(user);
    }

    @Test
    @DisplayName("정상적으로 댓글이 저장된다")
    void createComment_success() {
        // given
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Comment savedComment = commentService.createComment(1L, comment);

        // then
        assertThat(savedComment.getPost()).isEqualTo(post);
        assertThat(savedComment.getUser()).isEqualTo(user);
        assertThat(savedComment.getCommentContent()).isEqualTo("테스트 댓글");
    }

    @Test
    @DisplayName("게시글이 존재하지 않으면 예외가 발생한다")
    void createComment_postNotFound() {
        // given
        when(postRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(2L, comment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }
} 