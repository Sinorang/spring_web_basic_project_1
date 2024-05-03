package com.elice.boardproject.board.entity;

import com.elice.boardproject.acc.entity.User;
import com.elice.boardproject.post.entity.Post;
import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {
    private Long idx;
    @Setter
    private User user;
    private String name;
    private String description;
    private Timestamp date;

    public Board toEntity() {
        return Board.builder()
                .idx(idx)
                .user(user)
                .name(name)
                .description(description)
                .date(date)
                .build();
    }
}