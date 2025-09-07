package com.nsuslab.domain.user.repository;

import com.nsuslab.domain.user.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    Optional<User> findById(Long id);
    User save(User user);
    boolean existsByEmail(String email);
}
