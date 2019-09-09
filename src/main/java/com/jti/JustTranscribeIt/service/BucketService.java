package com.jti.JustTranscribeIt.service;

import com.jti.JustTranscribeIt.dao.*;
import com.jti.JustTranscribeIt.model.AudioFile;
import com.jti.JustTranscribeIt.model.Transcript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BucketService {

    @Autowired
    private UserUsageDao userUsageDao;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    private GeneratedUrlDao generatedUrlDao;

    @Autowired
    private TranscriptExplicitDao transcriptExplicitDao;

    private AmazonClientService amazonClientService;
    @Autowired
    BucketService(AmazonClientService amazonClientService) {
        this.amazonClientService = amazonClientService;
    }

    public Integer getUserMonthlyUsage(Integer userId) {
        return userUsageDao.getMonthlySum(userId);
    }

    public void uploadAndRecordFile(MultipartFile file, Integer userId, String userGivenName) {
        String fileUrl = uploadFile(file);
        Integer fileId = recordAudioFile(userId, fileUrl);
        recordEmptyTranscript(userId, userGivenName, fileId);
        startTranscriptJob(fileUrl, userGivenName);
    }

    private String uploadFile(MultipartFile file) {
        String fileUrl = this.amazonClientService.uploadFile(file);
        System.out.println("Audio file (" + fileUrl + ") uploaded to bucket!");
        return fileUrl;
    }

    private Integer recordAudioFile(Integer userId, String fileUrl) {
        AudioFile audioFile = new AudioFile(userId, fileUrl);
        audioFileDao.save(audioFile);
        System.out.println("Audio file (" + fileUrl + ") added to DB!");
        return audioFile.getId();
    }

    private void recordEmptyTranscript(Integer userId, String userGivenName, Integer fileId) {
        Transcript newTranscript = new Transcript(userId, userGivenName, fileId);
        transcriptDao.save(newTranscript);
        System.out.println("Created empty transcript: " + fileId);
    }

    private void startTranscriptJob(String fileUrl, String userGivenName) {
        amazonClientService.transcribeFile(fileUrl, userGivenName);
    }

    public void deleteTranscriptAndRecords(Integer transcriptId) {
        // Attempt to delete file from bucket
        Integer deletedFileId = getdDeletedFileId(transcriptId);
        String deletedFileUrl = getDeletedFileUrl(deletedFileId);
        Boolean wasDeletedFromBucket = this.amazonClientService.deleteFileFromS3Bucket(deletedFileUrl);
        // Attempt to delete records from database
        if (wasDeletedFromBucket) {
            deleteTranscriptRecords(transcriptId, deletedFileId, deletedFileUrl);
        }
    }

    private Integer getdDeletedFileId(Integer transcriptId) {
        return transcriptDao.findById(transcriptId).get().getFileId();
    }

    private String getDeletedFileUrl(Integer deletedFileId) {
        return audioFileDao.findById(deletedFileId).get().getFileUrl();
    }

    private void deleteTranscriptRecords(Integer transcriptId, Integer deletedFileId, String deletedFileUrl) {
        // Delete generated Urls
        Integer deleteGeneratedUrl = generatedUrlDao.deleteByTranscriptId(transcriptId);
        if (deleteGeneratedUrl == 0) {
            System.out.println("Failed to delete generated Url for " + deletedFileId + " from DB!");
        } else {
            System.out.println("Deleted generated Url for " + deletedFileId + " from DB!");
        }

        // Delete explicit transcript
        Integer deleteExplicitTranscript = transcriptExplicitDao.deleteByTranscriptId(transcriptId);
        if (deleteExplicitTranscript == 0) {
            System.out.println("Failed to delete explicit transcript for " + deletedFileId + " from DB!");
        } else {
            System.out.println("Deleted explicit transcript for " + deletedFileId + " from DB!");
        }

        // Delete transcript
        Integer deleteTranscript = transcriptDao.deleteByFileId(deletedFileId);
        if (deleteTranscript == 0) {
            System.out.println("Failed to delete transcript for " + deletedFileId + " from DB!");
        } else {
            System.out.println("Deleted transcript for " + deletedFileId + " from DB!");
        }

        // Delete audio file
        Integer deleteFile = audioFileDao.deleteByFileUrl(deletedFileUrl);
        if (deleteFile == 0) {
            System.out.println("Failed to delete audio file (" + deletedFileUrl + ") from DB!");
        } else {
            System.out.println("Deleted audio file (" + deletedFileUrl + ") from DB!");
        }
    }

    public Integer getFileCreatorId(Integer transcriptId) {
        return transcriptDao.findById(transcriptId).get().getUserId();
    }
}
