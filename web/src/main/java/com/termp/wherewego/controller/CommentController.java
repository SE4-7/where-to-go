package com.termp.wherewego.controller;

import com.termp.wherewego.model.Board;
import com.termp.wherewego.model.Comment;
import com.termp.wherewego.repository.BoardRepository;
import com.termp.wherewego.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/location")
public class CommentController {
    @Autowired
    private CommentRepository commentRepository;

    @PostMapping("/view")
    public String formSubmit(@ModelAttribute Comment comment){
        commentRepository.save(comment);
        return "redirect:/location/view?id="+comment.getLocation().getId();
    }

}
