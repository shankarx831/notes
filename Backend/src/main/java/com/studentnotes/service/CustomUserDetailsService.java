package com.studentnotes.service;

import com.studentnotes.config.CorrelationIdFilter;
import com.studentnotes.model.User;
import com.studentnotes.model.enums.UserStatus;
import com.studentnotes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom UserDetailsService that integrates with our User entity.
 * Handles user status checks and login tracking.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

        private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

        @Autowired
        private UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> {
                                        log.warn("Login attempt for non-existent user: {}", email);
                                        return new UsernameNotFoundException("User not found: " + email);
                                });

                // Check user status
                if (!user.canLogin()) {
                        log.warn("Login attempt for disabled user: {} (status: {})", email, user.getStatus());
                        throw new UsernameNotFoundException("Account is disabled: " + email);
                }

                // Set user ID in logging context
                CorrelationIdFilter.setUserId(user.getPublicId());

                // Convert user's role to a GrantedAuthority
                List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority(user.getRole()));

                // Create Spring Security UserDetails with account status flags
                return new org.springframework.security.core.userdetails.User(
                                user.getEmail(),
                                user.getPassword(),
                                user.getStatus() == UserStatus.ACTIVE, // enabled
                                true, // accountNonExpired
                                true, // credentialsNonExpired
                                user.getStatus() != UserStatus.SUSPENDED, // accountNonLocked
                                authorities);
        }

        /**
         * Updates the user's last login timestamp.
         * Should be called after successful authentication.
         */
        @Transactional
        public void updateLastLogin(String email) {
                userRepository.findByEmail(email).ifPresent(user -> {
                        user.setLastLoginAt(LocalDateTime.now());
                        userRepository.save(user);
                        log.debug("Updated last login for user: {}", email);
                });
        }
}