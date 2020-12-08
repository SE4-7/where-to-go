package com.termp.wherewego.controller;

import com.termp.wherewego.model.User;
import com.termp.wherewego.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String main(Model model){
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        String idu = a.getName();
        User user = userRepository.findById(idu).orElse(null);
        String[] choice = user.getUser_choice().split(",");
        ArrayList<String> params = new ArrayList<>();
        try{
            for(int i =0; i<choice.length; i++){
                if (choice[i].equals("비빔밥")){
                    params.add("0");
                }else if (choice[i].equals("치킨")){
                    params.add("1");
                }else if (choice[i].equals("햄버거")){
                    params.add("2");
                }else if (choice[i].equals("자장면")){
                    params.add("3");
                }else if (choice[i].equals("팟타이")){
                    params.add("4");
                }else if (choice[i].equals("피자")){
                    params.add("5");
                }else if (choice[i].equals("초밥")){
                    params.add("6");
                }else if (choice[i].equals("스프")){
                    params.add("7");
                }else if (choice[i].equals("스테이크")){
                    params.add("8");
                }else if (choice[i].equals("떡볶이")){
                    params.add("9");
                }
            }
            System.out.println(params);
        }catch (Exception e){

        }

        String result = CallMain.mainfunc(params.get(0),params.get(1),params.get(2));
        model.addAttribute("result",result);
        return "main";
    }
}
