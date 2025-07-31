package org.example.notearchive.service;

import org.example.notearchive.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLoginOrEmail(username, username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );
    }
}
