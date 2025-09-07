package com.nsuslab.domain.user;

import com.nsuslab.domain.user.model.User;
import com.nsuslab.domain.user.repository.UserRepository;
import com.nsuslab.domain.user.service.TokenService;
import com.nsuslab.domain.user.service.UserService;
import com.nsuslab.supports.error.CoreException;
import com.nsuslab.supports.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() {
        // given
        String email = "test@nsuslab.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(passwordEncoder.encode(password)).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.register(email, password);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_Fail_DuplicateEmail() {
        // given
        String email = "test@nsuslab.com";
        String password = "password123";
        given(userRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(email, password))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreEx = (CoreException) ex;
                    assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.DUPLICATE_EMAIL);
                });

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void authenticateUser_Success() {
        // given
        String email = "test@nsuslab.com";
        String password = "password123";
        User user = createUser(email, "encodedPassword");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
        given(userRepository.save(user)).willReturn(user);

        // when
        User result = userService.authenticateUser(email, password);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFailedLoginAttempts()).isEqualTo(0);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void authenticateUser_Fail_UserNotFound() {
        // given
        String email = "test@nsuslab.com";
        String password = "password123";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.authenticateUser(email, password))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreEx = (CoreException) ex;
                    assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.USER_NOT_FOUND);
                });

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 계정 잠김")
    void authenticateUser_Fail_AccountLocked() {
        // given
        String email = "test@nsuslab.com";
        String password = "password123";
        User lockedUser = createLockedUser(email, "encodedPassword");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(lockedUser));

        // when & then
        assertThatThrownBy(() -> userService.authenticateUser(email, password))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreEx = (CoreException) ex;
                    assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.ACCOUNT_LOCKED);
                });

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void authenticateUser_Fail_WrongPassword() {
        // given
        String email = "test@nsuslab.com";
        String password = "wrongPassword";
        User user = createUser(email, "encodedPassword");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);
        given(userRepository.save(user)).willReturn(user);

        // when & then
        assertThatThrownBy(() -> userService.authenticateUser(email, password))
                .isInstanceOf(CoreException.class)
                .satisfies(ex -> {
                    CoreException coreEx = (CoreException) ex;
                    assertThat(coreEx.getErrorType()).isEqualTo(ErrorType.INVALID_CREDENTIALS);
                    assertThat(coreEx.getMessage()).isEqualTo("비밀번호가 틀렸습니다");
                });

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("리프레시 토큰 업데이트")
    void updateRefreshToken_Success() {
        // given
        User user = createUser("test@nsuslab.com", "encodedPassword");
        String refreshToken = "refreshToken123";

        // when
        userService.updateRefreshToken(user, refreshToken);

        // then
        verify(tokenService).saveRefreshToken(user, refreshToken);
    }

    @Test
    @DisplayName("리프레시 토큰으로 사용자 찾기")
    void findByRefreshToken_Success() {
        // given
        String refreshToken = "refreshToken123";
        User expectedUser = createUser("test@nsuslab.com", "encodedPassword");

        given(tokenService.findUserByRefreshToken(refreshToken)).willReturn(Optional.of(expectedUser));

        // when
        Optional<User> result = userService.findByRefreshToken(refreshToken);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@nsuslab.com");
        verify(tokenService).findUserByRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("로그아웃")
    void logout_Success() {
        // given
        User user = createUser("test@nsuslab.com", "encodedPassword");

        // when
        userService.logout(user);

        // then
        verify(tokenService).clearRefreshToken(user);
    }

    @Test
    @DisplayName("사용자 ID로 찾기")
    void findById_Success() {
        // given
        Long userId = 1L;
        User expectedUser = createUser("test@nsuslab.com", "encodedPassword");

        given(userRepository.findById(userId)).willReturn(Optional.of(expectedUser));

        // when
        Optional<User> result = userService.findById(userId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@nsuslab.com");
        verify(userRepository).findById(userId);
    }


    private User createUser(String email, String password) {
        User user = new User(email, password);
        try {
            // BaseEntity에서 id 필드 찾기
            var idField = user.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private User createLockedUser(String email, String password) {
        User user = createUser(email, password);
        // 5번 실패하여 잠긴 상태로 만들기
        for (int i = 0; i < 5; i++) {
            user.incrementFailedAttempts();
        }
        return user;
    }
}