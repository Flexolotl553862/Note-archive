package org.example.notearchive.service;

import com.google.api.client.util.Lists;
import lombok.RequiredArgsConstructor;
import org.example.notearchive.domain.User;
import org.example.notearchive.dto.RegisterForm;
import org.example.notearchive.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User Register(RegisterForm registerForm) {
        User user = new User();
        user.setLogin(registerForm.getLogin());
        user.setRole(User.Role.ROLE_READER);
        user.setPasswordSha(passwordEncoder.encode(registerForm.getPassword()));
        user.setName(registerForm.getFullName());
        user.setEmail(registerForm.getEmail());
        userRepository.save(user);
        return user;
    }

    public List<User> getWritersExceptOne(User user) {
        List<User> users = Lists.newArrayList(userRepository.findByRole(User.Role.ROLE_WRITER));
        users.removeIf(u -> u.getId().equals(user.getId()));
        return users;
    }
}
