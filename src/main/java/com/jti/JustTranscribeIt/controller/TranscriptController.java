package com.jti.JustTranscribeIt.controller;

import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.TranscriptExplicitDao;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.service.AmazonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/transcribe")
public class TranscriptController {

    private AmazonClientService amazonClientService;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    private TranscriptExplicitDao transcriptExplicitDao;

    @Autowired
    TranscriptController(AmazonClientService amazonClientService) {
        this.amazonClientService = amazonClientService;
    }

    @Value("${my.urlRoot}")
    private String urlRoot;

    @PostMapping("/new")
    private String newTranscriptPage(@RequestParam(name = "fileUrl") String fileUrl,
                                     @RequestParam(name = "userGivenName") String userGivenName,
                                     Model model) {
        // Amazon Client asynchronously records new transcript in DB when transcription job is complete
        System.out.println("Transcribe request received - " + userGivenName);
        amazonClientService.transcribeFile(fileUrl, userGivenName);
        return "index";
    }

    @GetMapping("/new")
    private String newTranscriptPageGet(Model model) {
        return "transcribe";
    }

    @GetMapping("/view/{transcriptId}")
    private String viewTranscriptPageGet(@PathVariable(name = "transcriptId", required = true) Integer transcriptId,
                                         Model model) {
        // Add transcript to model
        Transcript transcript = transcriptDao.findById(transcriptId).get();
        model.addAttribute("transcript", transcript);

        return "transcription";
    }
}
