package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/")
    public String indexPage(Model model) {
        return "index";
    }

    @GetMapping("/delete")
    public String deletePage(Model model) {
        return "delete";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error, Model model) {
        if (error != null)
            model.addAttribute("error", error);

        return "login";
    }

    @GetMapping("/new-user")
    public String newUserPage(Model model) {
        return "new-user";
    }
}
