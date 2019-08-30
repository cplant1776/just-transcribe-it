package com.jti.JustTranscribeIt.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.jti.JustTranscribeIt.dao.*;
import com.jti.JustTranscribeIt.model.GeneratedUrl;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.service.AmazonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class PageController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private UserUsageDao userUsageDao;

    @Autowired
    private GeneratedUrlDao generatedUrlDao;

    @Autowired
    private AmazonClientService amazonClientService;

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

        // Check if logged-in user is authorized for this page
        User attemptedUser = userDao.findByUsername(getLoggedInUsername());
        if (attemptedUser == null || (attemptedUser.getId() != user.getId()) )
        {
            model.addAttribute("incorrectUser", true);
            return "account";
        } else {
            model.addAttribute("incorrectUser", false);
        }

        // Add user's transcripts to model
        List<Transcript> transcripts = transcriptDao.findByUserId(user.getId());
        model.addAttribute("transcripts", transcripts);
        // Get list of user's transcripts' ids
        List<Integer> transcriptIds = transcripts.stream().map(Transcript::getId).collect(Collectors.toList());

        // If non-expired link to audio file already exists, map it to urlMap
        HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
        if (!transcriptIds.isEmpty()) {
            urlMap = mapExistingGeneratedUrls(transcriptIds);
        }

        // If non-expired link doesn't exist, add its id to toGenerate
        List<Integer> toGenerate = new ArrayList<Integer>();
        if (!urlMap.isEmpty()) {
            for(Integer i: transcriptIds) {
                if(!urlMap.keySet().contains(i)) {
                    toGenerate.add(i);
                }
            }
        } else {
            // Must generate all links
            toGenerate = transcriptIds;
        }

        // Generate and map authorized Urls to user's audio files
        String authorizedUrl;
        if (!toGenerate.isEmpty()) {

//            if (urlMap.isEmpty()) { // case: all files must be generated
//                toGenerate = transcriptIds;
//            }

            for(Integer i: toGenerate) {
                // Map file url to transcript Id
                String fileUrl = audioFileDao.findById(transcriptDao.findById(i).get().getFileId()).get().getFileUrl();
                authorizedUrl = amazonClientService.getPresignedUrl(fileUrl);
                urlMap.put(i, authorizedUrl);
                // Save generated URL in DB
                generatedUrlDao.save(new GeneratedUrl(i, authorizedUrl));
            }
        }

        model.addAttribute("urlMap", urlMap);

        // Add user's monthly usage to model
        Integer monthUsage = userUsageDao.getMonthlySum(user.getId());
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

    private HashMap<Integer, String> mapExistingGeneratedUrls(List<Integer> ids) {
        HashMap<Integer, String> result = new HashMap<Integer, String>();
        // Generate map (transcriptId, GeneratedUrl) for URLs less than an hour old
        ArrayList<Object[]> queryResult = generatedUrlDao.mapGeneratedUrls(ids);
        for (Object[] res : queryResult) {
            result.put((Integer) res[0], (String) res[1]);
        }

        return Objects.requireNonNullElseGet(result, HashMap::new);

    }

}
