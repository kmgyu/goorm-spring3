package io.goorm.board.service;

import io.goorm.board.dto.LoginDto;
import io.goorm.board.dto.ProfileUpdateDto;
import io.goorm.board.dto.SignupDto;
import io.goorm.board.entity.Company;
import io.goorm.board.entity.User;
import io.goorm.board.exception.DuplicateEmailException;
import io.goorm.board.exception.InvalidCredentialsException;
import io.goorm.board.exception.UserNotFoundException;
import io.goorm.board.mapper.CompanyMapper;
import io.goorm.board.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    @Lazy
    private final PasswordEncoder passwordEncoder;
    
    public User signup(SignupDto signupDto) {
        log.info("회원가입 시도: email={}", signupDto.getEmail());
        
        // 이메일 중복 검사
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            log.warn("회원가입 실패 - 이메일 중복: email={}", signupDto.getEmail());
            throw new DuplicateEmailException(signupDto.getEmail());
        }
        
        // 비밀번호 확인 검사
        if (!signupDto.getPassword().equals(signupDto.getPasswordConfirm())) {
            log.warn("회원가입 실패 - 비밀번호 불일치: email={}", signupDto.getEmail());
            throw new IllegalArgumentException("Password mismatch");
        }
        
        // 사용자 엔티티 생성
        User user = new User();
        user.setEmail(signupDto.getEmail());
        String encodedPassword = passwordEncoder.encode(signupDto.getPassword());
        log.debug("비밀번호 암호화 완료: email={}, encodedLength={}", signupDto.getEmail(), encodedPassword.length());
        user.setPassword(encodedPassword); // BCrypt 암호화
        user.setNickname(signupDto.getNickname());
        
        User savedUser = userRepository.save(user);
        log.info("회원가입 성공: email={}, userId={}", signupDto.getEmail(), savedUser.getUserSeq());
        return savedUser;
    }
    
    @Transactional(readOnly = true)
    public User authenticate(LoginDto loginDto) {
        log.info("로그인 시도: email={}", loginDto.getEmail());
        
        Optional<User> userOptional = userRepository.findByEmail(loginDto.getEmail());
        
        if (userOptional.isEmpty()) {
            log.warn("로그인 실패 - 사용자 없음: email={}", loginDto.getEmail());
            throw new InvalidCredentialsException(loginDto.getEmail());
        }
        
        User user = userOptional.get();
        log.debug("사용자 찾음: email={}, storedPasswordLength={}", user.getEmail(), user.getPassword().length());
        
        // BCrypt 암호화된 비밀번호 검증
        boolean passwordMatches = passwordEncoder.matches(loginDto.getPassword(), user.getPassword());
        log.debug("비밀번호 검증 결과: email={}, matches={}", loginDto.getEmail(), passwordMatches);
        
        if (!passwordMatches) {
            log.warn("로그인 실패 - 비밀번호 불일치: email={}", loginDto.getEmail());
            throw new InvalidCredentialsException(loginDto.getEmail());
        }
        
        log.info("로그인 성공: email={}, userId={}", loginDto.getEmail(), user.getUserSeq());
        return user;
    }
    
    public User updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 현재 비밀번호 확인 (BCrypt 비교)
        if (!passwordEncoder.matches(profileUpdateDto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(user.getEmail());
        }
        
        // 닉네임 업데이트
        user.setNickname(profileUpdateDto.getNickname());
        
        // 새 비밀번호가 입력된 경우 비밀번호 변경
        if (profileUpdateDto.getNewPassword() != null && !profileUpdateDto.getNewPassword().isEmpty()) {
            // 새 비밀번호 확인 검사
            if (!profileUpdateDto.getNewPassword().equals(profileUpdateDto.getNewPasswordConfirm())) {
                throw new IllegalArgumentException("Password mismatch");
            }
            
            // BCrypt로 암호화하여 저장
            user.setPassword(passwordEncoder.encode(profileUpdateDto.getNewPassword()));
        }
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public User findByIdWithCompany(Long userId) {
        return userRepository.findByIdWithCompany(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    // UserDetailsService 구현 - Spring Security에서 사용
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Spring Security loadUserByUsername 호출: email={}", email);

        // 회사 정보를 포함해서 로드 (세션에서 사용할 때 LazyInitializationException 방지)
        User user = userRepository.findByEmailWithCompany(email)
                .orElseThrow(() -> {
                    log.warn("Spring Security - 사용자 없음: email={}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug("Spring Security - 사용자 로드 성공: email={}, authorities={}, companySeq={}",
                email, user.getAuthorities(), user.getCompanySeq());
        return user;
    }

    @Transactional(readOnly = true)
    public List<Company> findAllCompanies() {
        log.debug("모든 회사 조회");
        return companyMapper.findAllOrderByCompanyName();
    }
}