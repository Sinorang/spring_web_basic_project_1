package com.elice.boardproject.post.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.comment.entity.Comment;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", referencedColumnName = "user_idx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_idx", referencedColumnName = "board_idx")
    private Board board;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "post_date")
    @CreationTimestamp
    private LocalDateTime postDate;

    // 게시글 삭제 시 댓글도 함께 삭제
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 일관성 유지
    public void setBoard(Board board) {
        this.board = board;
        if (this.board != null && this.board.getPosts() != null && !this.board.getPosts().contains(this)) {
            this.board.getPosts().add(this);
        }
    }

    public Post(Board board, User user, String title, String content) {
        this.board = board;
        this.user = user;
        this.title = title;
        this.content = content;
    }
}
