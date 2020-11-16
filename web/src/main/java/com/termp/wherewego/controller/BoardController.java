package com.termp.wherewego.controller;

import com.termp.wherewego.model.Board;
import com.termp.wherewego.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    private BoardRepository boardRepository;
    @GetMapping("/list")
    public String list(Model model){
        List<Board> boards = boardRepository.findAll();
        // 모델에 "boards"란 변수를 만들어서 타임리프에서 사용할 수 있게 해주고
        // 그 변수에 boards변수(레포짓토리에서 가져온 값)의 값을 넣어줌
        model.addAttribute("boards",boards); // Thymeleaf에서 boards 변수를 사용 가능하게함
        return "board/list";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id){ // @RequestParam(required=false) -> false로 하여 무조건 받지 않아도 되게 설정
        if(id == null) {
            // 아이디값을 받지 ㅇ
            model.addAttribute("board",new Board());
        } else {
            Board board = boardRepository.findById(id).orElse(null); // .orElse()-> 아이디값이 없을경우를 지정
            model.addAttribute("board",board);
        }

        return "board/form";
    }

    @PostMapping("/form")
    public String formSubmit(@ModelAttribute Board board){
        boardRepository.save(board);
        return "redirect:/board/list";
    }

}
