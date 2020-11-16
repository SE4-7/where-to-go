package com.termp.wherewego.service;

import com.termp.wherewego.model.Emp;
import com.termp.wherewego.repository.EmpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmpServiceImpl implements EmpService{
    @Autowired
    private EmpRepository empRepository;
    @Override
    public List<Emp> findAll() {
        List<Emp> emps = new ArrayList<>();
        empRepository.findAll().forEach(e -> emps.add(e));
        return emps;
    }

    @Override
    public Emp findById(int empno) {
        return empRepository.findById(empno).orElse(null);
    }

    @Override
    public void deleteById(int empno) {
        empRepository.deleteById(empno);
    }

    @Override
    public Emp save(Emp emp) {
        return empRepository.save(emp);
    }

    @Override
    public List<Emp> findBySalBetween(int sal1, int sal2) {
        List<Emp> emps = empRepository.findBySalBetween(sal1, sal2);
        if (emps.size() > 0) return emps;
        else return null;
    }

    @Override
    public void updateById(int empno, Emp emp) {
        Emp e = empRepository.findById(empno).orElse(null);
        if (e != null){
            e.setEname(emp.getEname());
            e.setSal(emp.getSal());
            empRepository.save(emp);
        }
    }



}

