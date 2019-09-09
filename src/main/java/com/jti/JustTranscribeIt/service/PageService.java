package com.jti.JustTranscribeIt.service;

import com.jti.JustTranscribeIt.dao.*;
import com.jti.JustTranscribeIt.model.GeneratedUrl;
import com.jti.JustTranscribeIt.model.Transcript;
import com.jti.JustTranscribeIt.model.User;
import com.jti.JustTranscribeIt.utilities.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PageService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TranscriptDao transcriptDao;

    @Autowired
    private GeneratedUrlDao generatedUrlDao;

    @Autowired
    private AudioFileDao audioFileDao;

    @Autowired
    private UserUsageDao userUsageDao;

    @Autowired
    private AmazonClientService amazonClientService;


    public User getUserFromUsername(String username) {
        return userDao.findByUsername(username);
    }

    public User getUserFromId(Integer userId) {
        return userDao.findById(userId).get();
    }

    public List<Transcript> getTranscriptsByUserId(Integer userId) {
        return transcriptDao.findByUserId(userId);
    }

    public Integer getUserMonthlyUsage(Integer userId) {
        return userUsageDao.getMonthlySum(userId);
    }

    public HashMap<Integer, String> generateUrlMap(List<Transcript> transcripts) {
        HashMap<Integer, String> urlMap = new HashMap<Integer, String>();

        // Get list of user's transcripts' ids
        List<Integer> transcriptIds = transcripts.stream().map(Transcript::getId).collect(Collectors.toList());
        // Map any already-existing generated links
        if (!transcriptIds.isEmpty())
        { urlMap = mapExistingGeneratedUrls(transcriptIds); }
        // Get list of transcript id's that need new generated url
        if (!urlMap.isEmpty())
            { List<Integer> toGenerate = getTranscriptIdsToMap(transcriptIds, urlMap); }
        // Generate and map authorized Urls to user's audio files
        return generateAndMapUrls(urlMap, transcriptIds);
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

    private List<Integer> getTranscriptIdsToMap(List<Integer> transcriptIds, HashMap<Integer, String> urlMap) {
        List<Integer> toGenerate = new ArrayList<Integer>();
        if (!urlMap.isEmpty()) {
            for(Integer i: transcriptIds) {
                if(!urlMap.keySet().contains(i)) {
                    Status status = transcriptDao.findById(i).get().getStatus();
                    // Only generate if transcription job is finished
                    if (status == Status.COMPLETE)
                        toGenerate.add(i);
                }
            }
        } else {
            // Must generate all links
            toGenerate = transcriptIds;
        }

        return toGenerate;
    }

    private HashMap<Integer, String> generateAndMapUrls(HashMap<Integer, String> urlMap, List<Integer> toGenerate) {
        String authorizedUrl;
        if (!toGenerate.isEmpty()) {


            for(Integer i: toGenerate) {
                // Map file url to transcript Id
                String fileUrl = audioFileDao.findById(transcriptDao.findById(i).get().getFileId()).get().getFileUrl();
                authorizedUrl = amazonClientService.getPresignedUrl(fileUrl);
                urlMap.put(i, authorizedUrl);
                // Save generated URL in DB
                generatedUrlDao.save(new GeneratedUrl(i, authorizedUrl));
            }
        }
        return urlMap;
    }
}
