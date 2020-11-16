package com.termp.wherewego.repository;


import com.termp.wherewego.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
