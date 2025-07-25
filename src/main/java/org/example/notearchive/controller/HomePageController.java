package org.example.notearchive.controller;

import org.example.notearchive.domain.User;
import org.example.notearchive.repository.NoteRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("user")
public class HomePageController {

    private final NoteRepository noteRepository;

    HomePageController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @ModelAttribute
    public void initAll(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("notes", noteRepository.findAll());
        return "home";
    }

    @GetMapping("/my/notes")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_WRITER')")
    public String myNote(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = (User) userDetails;
        if (user.getRole() == User.Role.ROLE_ADMIN) {
            model.addAttribute("notes", noteRepository.findAll());
        } else {
            model.addAttribute("notes", noteRepository.findAllByAuthor_Login(userDetails.getUsername()));
        }
        return "my-notes";
    }
}
