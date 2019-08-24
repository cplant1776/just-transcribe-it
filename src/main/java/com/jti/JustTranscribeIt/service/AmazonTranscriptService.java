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
import com.jti.JustTranscribeIt.dao.AudioFileDao;
import com.jti.JustTranscribeIt.dao.TranscriptDao;
import com.jti.JustTranscribeIt.dao.TranscriptExplicitDao;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.TranscriptExplicit;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.transcribeClient = AmazonTranscribeClientBuilder.standard().
                withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public void listenForJobComplete(String transcriptionJobName, Integer fileId) {
        System.out.println("Waiting for job completion . . .");
        GetTranscriptionJobRequest jobRequest = new GetTranscriptionJobRequest();
        jobRequest.setTranscriptionJobName(transcriptionJobName);
        TranscriptionJob transcriptionJob;

        while (true) {
            // Keep checking if the job is  complete.
            // When complete, record transcript in DB
            transcriptionJob = transcribeClient.getTranscriptionJob(jobRequest).getTranscriptionJob();
            if (transcriptionJob.getTranscriptionJobStatus().equals(TranscriptionJobStatus.COMPLETED.name())) {

                try {
                    this.recordTranscription(transcriptionJob.getTranscript().getTranscriptFileUri(), fileId);
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

    private void recordTranscription(String uri, Integer fileId) throws IOException {
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

        // Read response as JSON
        JSONObject jsonObj = new JSONObject(res);

        // Parse JSON
        JSONObject results = jsonObj.getJSONObject("results");
        JSONArray items = results.getJSONArray("items");
        JSONArray transcripts = results.getJSONArray("transcripts");
        JSONObject transcriptJSON = transcripts.getJSONObject(0);

        // Extract job name, transcript, and explicit transcript
        String jobName = jsonObj.getString("jobName");
        String transcript = transcriptJSON.getString("transcript");
        String transcriptExplicit = items.toString();

        // Save basic transcript
        Transcript newTranscript = new Transcript(fileId, transcript, jobName);
        transcriptDao.save(newTranscript);
        System.out.println("Saved basic transcript | " + jobName);

        // Get id of newly saved transcript
        Integer newTranscriptId = transcriptDao.findByJobName(jobName).getId();
        // Save explicit transcript
        TranscriptExplicit newTranscriptExplicit = new TranscriptExplicit(newTranscriptId, transcriptExplicit);
        transcriptExplicitDao.save(newTranscriptExplicit);
        System.out.println("Saved explicit transcript | " + jobName);

    }
}
