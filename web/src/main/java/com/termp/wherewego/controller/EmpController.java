package com.termp.wherewego.controller;

import com.termp.wherewego.model.Emp;
import com.termp.wherewego.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("emp")
public class EmpController {
    @Autowired
    private EmpService empService;

    // 모든 사원 조회
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Emp>> getAllEmps(){
        List<Emp> emps = empService.findAll();
        return new ResponseEntity<List<Emp>>(emps, HttpStatus.OK);
    }

}
