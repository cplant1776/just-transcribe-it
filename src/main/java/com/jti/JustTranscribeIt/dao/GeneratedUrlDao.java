package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.GeneratedUrl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
public interface GeneratedUrlDao extends CrudRepository<GeneratedUrl, Integer> {

    @Query(value = "SELECT transcript_id, generated_url\n" +
            "FROM generatedurl\n" +
            "WHERE create_time > (NOW() - INTERVAL 1 HOUR)\n" +
            "AND transcript_id IN (:ids);",
            nativeQuery = true)
    public ArrayList<Object[]> mapGeneratedUrls(@Param("ids") List<Integer> ids);

    public Integer deleteByTranscriptId(Integer transcriptId);
}
