package com.jti.JustTranscribeIt.controller;


import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.jti.JustTranscribeIt.dao.AudioFileDao;
import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.AudioFile;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.service.AmazonClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

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

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file, Model model) throws IOException, UnsupportedAudioFileException {
        // Upload file to S3 bucket and get its destination url
        String fileUrl = this.amazonClientService.uploadFile(file);

        System.out.println("Audio file (" + fileUrl + ") uploaded to bucket!");

        // Get audio file duration
        Integer fileDuration = getAudioFileDuration(file);
        // Create new entry in audio_file table
        Integer loggedInId = getLoggedInId();
        AudioFile audioFile = new AudioFile(loggedInId, fileUrl, fileDuration);
        audioFileDao.save(audioFile);

        System.out.println("Audio file (" + fileUrl + ") added to DB!");

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

    private Integer getAudioFileDuration(MultipartFile multipartFile) throws IOException, UnsupportedAudioFileException {
        File file = convertMultipartFileToFile(multipartFile);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        long audioFileLength = file.length();
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        Integer durationInSeconds = Math.round(audioFileLength / (frameSize * frameRate));

        return durationInSeconds;
    }

    public static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();

        if(! new File(uploadDir).exists())
        {
            new File(uploadDir).mkdir();
        }

        File destDir = new File(uploadDir);


        File dest = File.createTempFile("jti-", null, destDir);
        dest.deleteOnExit();
        file.transferTo(dest);

        return dest;
    }

}
