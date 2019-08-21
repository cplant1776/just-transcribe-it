package com.jti.JustTranscribeIt.dao;

import com.jti.JustTranscribeIt.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserDao extends CrudRepository<User, Integer> {
    User findByUsername(String username);
}
