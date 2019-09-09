package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/")
    public String indexPage(Model model) {
        return "index";
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
        User user = pageService.getUserFromUsername(getLoggedInUsername());
        if (user != null)
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
        User user = pageService.getUserFromId(id);
        model.addAttribute("user", user);

        // Check if logged-in user is authorized for this page
        User attemptedUser = pageService.getUserFromUsername(getLoggedInUsername());
        if (attemptedUser == null || (attemptedUser.getId() != user.getId()) )
        {
            model.addAttribute("incorrectUser", true);
            return "account";
        } else {
            model.addAttribute("incorrectUser", false);
        }

        // Add user's transcripts to model
        List<Transcript> transcripts = pageService.getTranscriptsByUserId(user.getId());
        model.addAttribute("transcripts", transcripts);

        // Generate url map and add it to the model
        HashMap<Integer, String> urlMap = pageService.generateUrlMap(transcripts);
        model.addAttribute("urlMap", urlMap);

        // Add user's monthly usage to model
        Integer monthUsage = pageService.getUserMonthlyUsage(user.getId());
        model.addAttribute("monthUsage", monthUsage);

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
