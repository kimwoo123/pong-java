package com.example.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class firstContoroller {

    @GetMapping("/greet")
    public String niceToMeetYou(Model model) {
        model.addAttribute("username", "tosh");
        return "greetings"; // templates/greetings.mustache
    }

    @GetMapping("/relation")
    public String seeYouNext(Model model) {
        model.addAttribute("nickname", "hong gil dong");
        return "goodbye";
    }
}
