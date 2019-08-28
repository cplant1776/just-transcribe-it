package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PageController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TranscriptDao transcriptDao;

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

    @GetMapping("/account")
    public String accountRedirect(Model model) {
        // Get logged-in userId
        User user = userDao.findByUsername(getLoggedInUsername());
//        User user = userDao.findByUsername("jc");
        if (user!= null)
        {
            Integer userId = user.getId();
            return "redirect:" + "/account/" + userId;
        }
        else
            return "redirect:" + "/login";

    }

    @GetMapping("/account/{userId}")
    public String userPage(@PathVariable(name = "userId", required = true) Integer id, Model model) {
        // Add user to model
        User user = userDao.findById(id).get();
        model.addAttribute("user", user);
        // Add user's transcripts to model
        List<Transcript> transcripts = transcriptDao.findByUserId(user.getId());
        model.addAttribute("transcripts", transcripts);

        // Check if logged-in user is authorized for this page
        User attemptedUser = userDao.findByUsername(getLoggedInUsername());
//        User attemptedUser = userDao.findByUsername("jc");
        if (attemptedUser == null || user == null)
        {
            model.addAttribute("incorrectUser", true);
            return "account";
        }

        if (attemptedUser.getId() != user.getId())
            model.addAttribute("incorrectUser", true);
        else
            model.addAttribute("incorrectUser", false);

        return "account";
    }

    private String getLoggedInUsername() {
        // Get username of logged-in user
        String username;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();

        } else {
            username = principal.toString();
        }

        return username;
    }

}
