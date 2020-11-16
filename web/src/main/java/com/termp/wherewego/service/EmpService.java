package com.termp.wherewego.service;

import com.termp.wherewego.model.Emp;

import java.util.List;

public interface EmpService {
    // 모든 사원 주세요
    List<Emp> findAll();
    // 하나의 사원주세요
    Emp findById(int empno);
    // 이 사원번호의 사원 삭제
    void deleteById(int empno);
    // 사원 추가
    Emp save(Emp emp);
    // 레파지토리에서 만든거
    List<Emp> findBySalBetween(int sal1, int sal2);
    // 수정
    void updateById(int empno, Emp emp);
}
