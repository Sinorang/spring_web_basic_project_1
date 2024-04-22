package com.elice.boardproject.board.repository;

import com.elice.boardproject.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
