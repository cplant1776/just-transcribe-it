package com.jti.JustTranscribeIt.controller;


import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jti.JustTranscribeIt.dao.AudioFileDao;
import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.AudioFile;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.service.AmazonClientService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/storage")
public class BucketController {

    private AmazonClientService amazonClientService;
    private static String uploadDir = Paths.get(System.getProperty("user.dir"), "src", "main", "tmp").toString();

    @Autowired
    private UserDao userDao;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    BucketController(AmazonClientService amazonClientService) {
        this.amazonClientService = amazonClientService;
    }

    @Value("${my.urlRoot}")
    private String urlRoot;

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file,
                             @RequestParam(name = "userGivenName") String userGivenName,
                             Model model) throws IOException, UnsupportedAudioFileException {
        // Upload file to S3 bucket and get its destination url
        String fileUrl = this.amazonClientService.uploadFile(file);
        System.out.println("Audio file (" + fileUrl + ") uploaded to bucket!");

        // Create new entry in audio_file table
        Integer loggedInId = getLoggedInId();
        AudioFile audioFile = new AudioFile(loggedInId, fileUrl);
        audioFileDao.save(audioFile);
        System.out.println("Audio file (" + fileUrl + ") added to DB!");

        // Start asynchronous transcription of uploaded file
        amazonClientService.transcribeFile(fileUrl, userGivenName);

        return "index";
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "fileUrl") String fileUrl) {
        // Delete file at destination url from bucket
        Boolean wasDeleted = this.amazonClientService.deleteFileFromS3Bucket(fileUrl);
        // Get deleted file ID
        Integer deletedFileId = audioFileDao.findByFileUrl(fileUrl).getId();

        // Delete file from DB
        if(wasDeleted) {
            // Delete audio file
            Integer deleteFile = audioFileDao.deleteByFileUrl(fileUrl);
            if (deleteFile == 0) {
                System.out.println("Failed to delete audio file (" + fileUrl + ") from DB!");
            } else {
                System.out.println("Deleted audio file (" + fileUrl + ") from DB!");
            }

            // Delete audio file transcripts
            Integer deleteTranscript = transcriptDao.deleteByFileId(deletedFileId);
            if (deleteFile == 0) {
                System.out.println("Failed to delete transcript for " + deletedFileId + " from DB!");
            } else {
                System.out.println("Deleted transcript for " + deletedFileId + " from DB!");
            }
        }

        return "index";
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

    private Integer getLoggedInId() {
        String username = getLoggedInUsername();
        if (username.equals("anonymousUser"))
            return -1;
        User user = userDao.findByUsername(username);
        return user.getId();
    }

//    private Integer getAudioFileDuration(MultipartFile multipartFile) throws IOException, UnsupportedAudioFileException {
//        File file = convertMultipartFileToFile(multipartFile);
//        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
//        AudioFormat format = audioInputStream.getFormat();
//        long audioFileLength = file.length();
//        int frameSize = format.getFrameSize();
//        float frameRate = format.getFrameRate();
//        Integer durationInSeconds = Math.round(audioFileLength / (frameSize * frameRate));
//
//        return durationInSeconds;
//    }

//    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
//        File convFile = new File(file.getOriginalFilename());
//        convFile.createNewFile();
//        FileOutputStream fos = new FileOutputStream(convFile);
//        fos.write(file.getBytes());
//        fos.close();
//
//        if(! new File(uploadDir).exists())
//        {
//            new File(uploadDir).mkdir();
//        }
//
//        File destDir = new File(uploadDir);
//
//
//        File dest = File.createTempFile("jti-", null, destDir);
//        dest.deleteOnExit();
//        file.transferTo(dest);
//
//        return dest;
//    }


//    public List<String> getUserCredentials()
//    {
//        String username = "";
//        String password = "";
//        // Get logged in username and password
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (principal instanceof UserDetails) {
//            username = ((UserDetails)principal).getUsername();
//            password = ((UserDetails)principal).getPassword();
//
//        } else {
//            username = principal.toString();
//        }
//        return Arrays.asList(username, password);
//    }

}
