package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.AudioFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AudioFileDao extends CrudRepository<AudioFile, Integer> {

    public AudioFile findByFileUrl(String fileUrl);

    public Integer deleteByFileUrl(String fileUrl);
}
