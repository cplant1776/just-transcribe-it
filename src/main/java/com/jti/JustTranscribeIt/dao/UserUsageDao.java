package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.UserUsage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserUsageDao extends CrudRepository<UserUsage, Integer> {
}
