package com.jti.JustTranscribeIt.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.transcribe.AmazonTranscribe;
import com.amazonaws.services.transcribe.AmazonTranscribeClientBuilder;
import com.amazonaws.services.transcribe.model.GetTranscriptionJobRequest;
import com.amazonaws.services.transcribe.model.TranscriptionJob;
import com.amazonaws.services.transcribe.model.TranscriptionJobStatus;
import com.jti.JustTranscribeIt.dao.*;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.TranscriptExplicit;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.model.UserUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AmazonTranscriptService {

    @Value("${amazonProperties.accessKey}")
    private String accessKey;
    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    private AmazonTranscribe transcribeClient;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    private TranscriptExplicitDao transcriptExplicitDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private UserUsageDao userUsageDao;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.transcribeClient = AmazonTranscribeClientBuilder.standard().
                withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public void listenForJobComplete(String transcriptionJobName, Integer fileId, Integer userId, String userGivenName) {
        System.out.println("Waiting for job completion . . .");
        GetTranscriptionJobRequest jobRequest = new GetTranscriptionJobRequest();
        jobRequest.setTranscriptionJobName(transcriptionJobName);
        TranscriptionJob transcriptionJob;

        while (true) {
            // Keep checking if the job is  complete.
            // When complete, update DB
            transcriptionJob = transcribeClient.getTranscriptionJob(jobRequest).getTranscriptionJob();
            if (transcriptionJob.getTranscriptionJobStatus().equals(TranscriptionJobStatus.COMPLETED.name())) {

                try {
                    this.recordTranscription(transcriptionJob.getTranscript().getTranscriptFileUri(),
                            fileId, userId, userGivenName);
                    System.out.println("Transcription complete!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            } else if (transcriptionJob.getTranscriptionJobStatus().equals(TranscriptionJobStatus.FAILED.name())) {

                break;
            }
            // to not be so anxious
            synchronized (this) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                }
            }

        }
    }

    private void recordTranscription(String uri, Integer fileId, Integer userId, String userGivenName) throws IOException {
        // Get job response as String
        String res = getResponseAsString(uri);
        // Read response into JSON object
        JSONObject jsonObj = new JSONObject(res);


        // Parse JSON
        JSONObject results = jsonObj.getJSONObject("results");
        JSONArray items = results.getJSONArray("items");
        JSONArray transcripts = results.getJSONArray("transcripts");
        JSONObject transcriptJSON = transcripts.getJSONObject(0);
        // Extract job name, transcript, explicit transcript, and duration from JSON
        String jobName = jsonObj.getString("jobName");
        String transcript = transcriptJSON.getString("transcript");
        String transcriptExplicit = items.toString();
        Integer transcriptLength = getTranscriptLength(items);

        // Save basic transcript
        saveBasicTranscript(fileId, transcript, jobName, userId, userGivenName);
        // Get id of newly saved transcript
        Integer newTranscriptId = transcriptDao.findByJobName(jobName).getId();

        // Save explicit transcript
        saveExplicitTranscript(newTranscriptId, transcriptExplicit);

        // Save user usage for transcript
        saveUserUsage(userId, transcriptLength, newTranscriptId);
    }

    private String getResponseAsString(String uri) throws IOException {
        // Send GET request to job - returns JSON object
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Read the object into a string
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        String res = content.toString();
        in.close();

        return res;
    }

    private Integer getTranscriptLength(JSONArray items) {
        // Had issues with creating AudioInputStream from files, so extracting duration from AWS response instead
        // This is something I may revisit in the future

        // Pull end time from end time of last mapped word
        JSONObject endJSON = items.getJSONObject(items.length()-2);
        return endJSON.getInt("end_time");
    }

    private void saveBasicTranscript(Integer fileId, String transcript, String jobName,
                                     Integer userId, String userGivenName) {
        Transcript newTranscript = new Transcript(fileId, transcript, jobName, userId, userGivenName);
        transcriptDao.save(newTranscript);
        System.out.println("Saved basic transcript | file id = " + fileId);
    }

    private void saveExplicitTranscript(Integer newTranscriptId, String transcriptExplicit) {
        TranscriptExplicit newTranscriptExplicit = new TranscriptExplicit(newTranscriptId, transcriptExplicit);
        transcriptExplicitDao.save(newTranscriptExplicit);
        System.out.println("Saved explicit transcript | transcript id = " + newTranscriptExplicit);

    }

    private void saveUserUsage(Integer userId, Integer transcriptLength, Integer transcriptId) {
        // Minimum length for transcript length - 15 seconds
        if (transcriptLength < 15)
            transcriptLength = 15;

        // Save new usage record
        UserUsage usage = new UserUsage(userId, transcriptLength, transcriptId);
        userUsageDao.save(usage);
        System.out.println("Saved user usage | transcript id = " + transcriptId);
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
