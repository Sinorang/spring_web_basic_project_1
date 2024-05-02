package com.elice.boardproject.board.entity;

import com.elice.boardproject.acc.entity.User;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {
    private Long idx;
    private String name;
    private String description;
    private Timestamp date;

    public Board toEntity() {
        return Board.builder()
                .idx(idx)
                .name(name)
                .description(description)
                .date(date)
                .build();
    }
}