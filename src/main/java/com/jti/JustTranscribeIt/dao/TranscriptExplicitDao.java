package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.TranscriptExplicit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface TranscriptExplicitDao extends CrudRepository<TranscriptExplicit, Integer> {
}
