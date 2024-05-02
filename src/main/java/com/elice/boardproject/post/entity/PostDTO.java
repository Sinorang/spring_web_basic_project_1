package com.elice.boardproject.post.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.board.entity.Board;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private Long userIdx; // User의 userIdx
    private String nickname; // User의 nickname

    private Long boardIdx; // Board의 boardIdx

    private Long id;
    private String title;
    private String content;
    private Timestamp postDate;


    public Post toEntity() {
        User user = new User();
        user.setIdx(userIdx);
        user.setNickname(nickname);

        Board board = new Board();
        board.setIdx(boardIdx);

        return Post.builder()
                .user(user)
                .board(board)
                .id(id)
                .title(title)
                .postDate(postDate)
                .build();
    }
}