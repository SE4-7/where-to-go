package com.termp.wherewego.repository;

import com.termp.wherewego.model.Emp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpRepository extends JpaRepository<Emp, Integer> {
    List<Emp> findBySalBetween(int sal, int sal2);
}
