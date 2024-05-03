package com.elice.boardproject.board.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.post.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.sql.Timestamp;
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
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_idx")
    private Long idx;

    @ManyToOne // 유저 : 게시판 = 1 : N
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL) // 게시판 : 게시글 = 1 : N
    private List<Post> posts = new ArrayList<>();

    @Column(name = "board_name", nullable = false)
    private String name;

    @Column(name = "board_des", nullable = false)
    private String description;

    @Column(name = "board_date")
    @CreationTimestamp
    private Timestamp date;

    public Board(Long idx, User user, String name, String description){
        this.idx = idx;
        this.user = user;
        this.name = name;
        this.description = description;
    }
}
