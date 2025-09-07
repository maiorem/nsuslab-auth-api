package com.nsuslab.domain.user.service;

import com.nsuslab.domain.user.model.User;
import com.nsuslab.domain.user.repository.UserRepository;
import com.nsuslab.supports.error.CoreException;
import com.nsuslab.supports.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new CoreException(ErrorType.DUPLICATE_EMAIL);
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(ErrorType.USER_NOT_FOUND));

        if (user.isAccountLocked()) {
            throw new CoreException(ErrorType.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw new CoreException(ErrorType.INVALID_CREDENTIALS, "비밀번호가 틀렸습니다");
        }

        user.resetFailedAttempts();
        return userRepository.save(user);
    }

    public void updateRefreshToken(User user, String refreshToken) {
        tokenService.saveRefreshToken(user, refreshToken);
    }

    public Optional<User> findByRefreshToken(String refreshToken) {
        return tokenService.findUserByRefreshToken(refreshToken);
    }

    public void logout(User user) {
        tokenService.clearRefreshToken(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}
