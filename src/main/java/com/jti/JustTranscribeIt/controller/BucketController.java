package com.jti.JustTranscribeIt.controller;


import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;


@Controller
@RequestMapping("/storage")
public class BucketController {


    @Autowired
    private UserDao userDao;

    @Autowired
    private BucketService bucketService;

    @Value("${my.urlRoot}")
    private String urlRoot;

    @Value("${my.usageLimitMonthly}")
    private Integer monthlyUsageLimit;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file,
                             @RequestParam(name = "userGivenName") String userGivenName,
                             Model model) {
        // Check user monthly usage
        Integer loggedInId = getLoggedInId();
        Integer monthlyUsage = bucketService.getUserMonthlyUsage(loggedInId);

        if (monthlyUsage >= monthlyUsageLimit)
        {
            System.out.println("User " + loggedInId + " usage limit exceeded.");
            model.addAttribute("messageType", "Error Encountered");
            model.addAttribute("messageBody", "You have reached your usage limit for this month. Please try again next month!");
            return "message";
        }
        else
        {
            bucketService.uploadAndRecordFile(file, loggedInId, userGivenName);
            return "index";
        }


    }

    @DeleteMapping("/deleteFile")
    @ResponseBody
    public String delete(@RequestParam(value = "transcriptId") Integer transcriptId, Model model) throws AuthenticationException {
        // Check if user is authorized
        Integer loggedInId = getLoggedInId();
        Integer fileCreatorId = bucketService.getFileCreatorId(transcriptId);
        if (loggedInId != fileCreatorId)
            throw new AuthenticationException("Logged in ID doesn't match creator id: ("
                                                + loggedInId + "| " + fileCreatorId + ")");

        // Delete file from S3 bucket then remove records in database
        bucketService.deleteTranscriptAndRecords(transcriptId);

        return "Deleted" + transcriptId;
    }



    /*=========================
        UTILITY FUNCTIONS
     =========================*/
    public Integer getLoggedInId() {
        String username = getLoggedInUsername();
        if (username.equals("anonymousUser"))
            return -1;
        User user = userDao.findByUsername(username);
        return user.getId();
    }


    public String getLoggedInUsername() {
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
