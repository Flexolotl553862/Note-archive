package org.example.notearchive.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.notearchive.dto.RegisterForm;
import org.example.notearchive.service.UserService;
import org.example.notearchive.validator.RegisterFormValidator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterPageController {
    private final RegisterFormValidator formValidator;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final AuthenticationManager authenticationManager;

    public RegisterPageController(
            RegisterFormValidator formValidator,
            UserService userService,
            AuthenticationManager authenticationManager
    ) {
        this.formValidator = formValidator;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @ModelAttribute
    public void addRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
    }

    @GetMapping("/register")
    public String getRegisterPage(Authentication auth) {
        if (isAuthenticated(auth)) {
            return "redirect:/home";
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            Authentication auth,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (isAuthenticated(auth)) {
            return "redirect:/home";
        }
        formValidator.validate(registerForm, bindingResult);
        if (bindingResult.hasErrors()) {
            return "register";
        }
        userService.Register(registerForm);
        SecurityContext context = SecurityContextHolder.getContextHolderStrategy().createEmptyContext();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(registerForm.getLogin(), registerForm.getPassword());
        context.setAuthentication(authenticationManager.authenticate(token));
        securityContextRepository.saveContext(context, request, response);
        return "redirect:/home";
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && (auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken);
    }
}
