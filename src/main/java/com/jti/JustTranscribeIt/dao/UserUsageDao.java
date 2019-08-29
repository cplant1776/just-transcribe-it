package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.UserUsage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserUsageDao extends CrudRepository<UserUsage, Integer> {

    @Query(value = "SELECT SUM(transcript_length) SumTotalAmount\n" +
            "FROM justtranscribeit.userusage\n" +
            "WHERE\n" +
            "YEAR(create_time) = YEAR(CURRENT_DATE())\n" +
            "AND\n" +
            "MONTH(create_time) = MONTH(CURRENT_DATE())\n" +
            "AND\n" +
            "user_id in (:userId);", nativeQuery = true)
    public Integer getMonthlySum(@Param("userId")Integer userId);
}
