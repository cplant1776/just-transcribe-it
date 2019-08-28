package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.Transcript;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface TranscriptDao extends CrudRepository<Transcript, Integer> {
    public Integer deleteByFileId(Integer fileId);
    public Transcript findByJobName(String jobName);
    public Transcript findByFileId(Integer fileId);
    public List<Transcript> findByUserId(Integer userId);
}
