package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/new-user")
    public String newUserPage(@RequestParam(name = "username", required = true) String username,
                              @RequestParam(name = "password", required = true) String password,
                              @RequestParam(name = "confirmPassword", required = true) String confirmPassword,
                              Model model)
    {
        User existingUser = userDao.findByUsername(username);
        if (existingUser != null)
        {
            model.addAttribute("error", "Username already exists. Please try a different name.");
            return "new-user";
        }
        else if(!password.equals(confirmPassword))
        {
            model.addAttribute("error", "Passwords do not match. Please try again.");
            return "new-user";
        }
        else
        {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));

            userDao.save(user);
            System.out.println("Added user successfully!");

            return "index";
        }

    }

    @PutMapping("/new-user")
    private String updatePassword(@RequestParam("currentPassword") String currentPassword,
                                  @RequestParam("newPassword") String newPassword,
                                  @RequestParam("confirmNewPassword") String confirmNewPassword,
                                  @RequestParam("userId") Integer userId,
                                  Model model)
    {
        String errorMsg = "";
        User user = userDao.findById(userId).get();

        // New password wasnt entered identically twice...they didnt match
        if (!newPassword.equals(confirmNewPassword))
        {
            errorMsg = "The new passwords you entered do not match! Try again.";
            model.addAttribute("errorMsg", errorMsg);
        }
        // Old password didn't match user's password in the database
        else if (!passwordEncoder.encode(currentPassword).equals(user.getPassword()))
        {
            errorMsg = "Your old password was not correct! Try again.";
            model.addAttribute("errorMsg", errorMsg);
        }
        return "redirect:/account/" + userId;
    }

}
