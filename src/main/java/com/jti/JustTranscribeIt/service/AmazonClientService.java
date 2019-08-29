package com.jti.JustTranscribeIt.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClient;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.*;
import com.jti.JustTranscribeIt.dao.AudioFileDao;
import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.TranscriptExplicitDao;
import com.jti.JustTranscribeIt.dao.UserDao;
import com.jti.JustTranscribeIt.model.AudioFile;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.TranscriptExplicit;
import com.jti.JustTranscribeIt.model.User;
import org.apache.tomcat.util.json.JSONParser;
import org.aspectj.weaver.ast.Call;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.util.Date;
import java.util.Objects;

@Service
public class AmazonClientService {

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;
    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    private AmazonS3 s3client;

    private AmazonTranscribe transcribeClient;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private AmazonTranscriptService amazonTranscriptService;

    @Autowired
    private UserDao userDao;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
        this.transcribeClient = AmazonTranscribeClientBuilder.standard().
                withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    /*================================
           BUCKET FUNCTIONS
     =================================*/

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
    }

    public Boolean deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        try {
            DeleteObjectsRequest delObjReq = new DeleteObjectsRequest(bucketName)
                    .withKeys(fileName);
            s3client.deleteObjects(delObjReq);
            System.out.println("Deleted " + fileUrl + " from bucket.");
        } catch (SdkClientException e) {
            return false;
        }
        s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
        return true;
    }

    /*================================
           TRANSCRIBE FUNCTIONS
     =================================*/

    @Async
    public void transcribeFile(String fileUrl, String userGivenName) {
        // Create request
        StartTranscriptionJobRequest req = new StartTranscriptionJobRequest();
        // Set language as english
        req.withLanguageCode(LanguageCode.EnUS);
        // Create media object for audio file
        Media media = new Media();
        media.setMediaFileUri(fileUrl);
        // Add media object to request
        req.withMedia(media);

        // Set job name
        String transcriptionJobName = generateJobName();
        req.setTranscriptionJobName(transcriptionJobName);
        // Set file format
        String extension = fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
        req.setMediaFormat(extension);

        // Start transcribe job
        this.transcribeClient.startTranscriptionJob(req);

        // Get file Id and logged in user Id
        Integer fileId = audioFileDao.findByFileUrl(fileUrl).getId();
        Integer userId = getLoggedInId();

        // Start listener to check for when job is done
        new Thread(new Runnable() {
            public void run() {
                amazonTranscriptService.listenForJobComplete(transcriptionJobName, fileId, userId, userGivenName);
            }
        }).start();
    }

    public String getPresignedUrl(String fileUrl) {
        URL url = null;
        try {
            // Set the presigned URL to expire after one hour.
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 60;
            expiration.setTime(expTimeMillis);

            // Get object key from url
            AmazonS3URI amazonS3URI = new AmazonS3URI(fileUrl);
            String objectKey = amazonS3URI.getKey();

            // Generate the presigned URL.
            System.out.println("Generating pre-signed URL.");
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, objectKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            url = this.s3client.generatePresignedUrl(generatePresignedUrlRequest);

            System.out.println("Pre-Signed URL: " + url.toString());
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }

        return Objects.requireNonNull(url).toString();
    }

        /*================================
           UTILITY FUNCTIONS
     =================================*/

    public String uploadFile(MultipartFile multipartFile) {

        String fileUrl = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
            uploadFileTos3bucket(fileName, file);
            System.out.println("Uploaded " + fileUrl + " to bucket.");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
    }


    private String generateJobName() {
        return new Date().getTime() + "";
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

}