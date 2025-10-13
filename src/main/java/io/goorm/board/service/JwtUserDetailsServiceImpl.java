package io.goorm.board.service;

import io.goorm.board.exception.UserNotFoundException;
import io.goorm.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component("jwtUserDetailsService")
@RequiredArgsConstructor
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("JWT 인증을 위한 사용자 조회: {}", username);

        return userRepository.findByEmail(username)
            .orElseThrow(() -> {
                log.warn("JWT 인증 실패 - 사용자를 찾을 수 없음: {}", username);
                throw new UserNotFoundException(username);
            });
    }
}