package com.elice.boardproject.post.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.board.entity.Board;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
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
    private Timestamp postDate;

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
