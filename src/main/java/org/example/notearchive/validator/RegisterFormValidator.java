package org.example.notearchive.validator;

import org.example.notearchive.dto.RegisterForm;
import org.example.notearchive.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegisterFormValidator implements Validator {
    UserRepository userRepository;

    public RegisterFormValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return RegisterForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterForm form = (RegisterForm) target;
        if (errors.hasErrors()) {
            return;
        }
        if (userRepository.existsByName(form.getFullName())) {
            errors.rejectValue("firstName", "person.already.exists", "Person already exists");
            errors.rejectValue("lastName", "person.already.exists", "Person already exists");
        }
        if (userRepository.existsByLogin(form.getLogin())) {
            errors.rejectValue("login", "login.already.exists", "Login already exists");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            errors.rejectValue("email", "email.already.exists", "Email already exists");
        }
    }
}
