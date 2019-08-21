package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.Transcript;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TranscriptDao extends CrudRepository<Transcript, Integer> {
}
