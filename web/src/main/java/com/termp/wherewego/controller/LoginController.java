package com.termp.wherewego.controller;

import com.termp.wherewego.model.User;
import com.termp.wherewego.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {
    @Autowired // 스프링이 자동으로 인스턴스를 넣어줌
    private UserService userService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping("/login")
    public void logging(){}

    @PostMapping("/register")
    public String register(User user){
        userService.save(user);
        return "redirect:/login";
    }
}
