package com.elice.boardproject.board.repository;

import com.elice.boardproject.board.entity.Board;
import com.elice.boardproject.board.entity.BoardDTO;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 게시판 조회
    List<Board> findAll();

    Board save(Board board); //새로운 엔티티이면 저장되고 이미 존재하면 업데이트 됨. 저장된 엔티티를 반환한다.

    @Modifying
    @Query("DELETE FROM Board b WHERE b.idx = ?1")
    void deleteById(Long boardIdx);
}