package org.example.notearchive.controller;

import org.example.notearchive.dto.LoginForm;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginPageController {

    @ModelAttribute
    public void addLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
    }

    @GetMapping("/login")
    public String getLoginPage(@RequestParam(required = false) String error, Model model, Authentication auth) {
        if (isAuthenticated(auth)) {
            return "redirect:/home";
        }
        if (error != null) {
            BindingResult bindingResult = new BeanPropertyBindingResult(new LoginForm(), "loginForm");
            bindingResult.rejectValue("loginOrEmail", "error.login", "Invalid login/email or password");
            model.addAttribute("org.springframework.validation.BindingResult.loginForm", bindingResult);
        }
        return "login";
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && (auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken);
    }
}
