package com.abarigena.authenticationservice.security;

import com.abarigena.authenticationservice.entity.AuthUser;
import com.abarigena.authenticationservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthUserRepository userRepository;

    @Override
    @Transactional(readOnly = true) // Важно для загрузки LAZY коллекций, если они есть
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

        // AuthUser уже реализует UserDetails
        return user;
    }
}