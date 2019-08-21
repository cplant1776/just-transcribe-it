package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/")
    public String indexPage(Model model) {
        User user = userDao.findByUsername("jc");
        System.out.println(user.getId());
        return "index";
    }
}
