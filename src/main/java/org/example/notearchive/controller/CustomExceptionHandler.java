package org.example.notearchive.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String exceptionHandler(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/not/found";
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public String handleMissingPathVariableException(MissingPathVariableException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", "No such " + ex.getVariableName());
        return "redirect:/not/found";
    }
}
