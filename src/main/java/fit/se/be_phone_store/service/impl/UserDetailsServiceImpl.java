package fit.se.be_phone_store.service.impl;

import fit.se.be_phone_store.entity.User;
import fit.se.be_phone_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User Details Service Implementation
 * Loads user details for Spring Security authentication
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.debug("Loading user by identifier: {}", identifier);

        User user = userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier)));

        log.debug("User found: {} with role: {}", user.getUsername(), user.getRole());

        return user;
    }
}