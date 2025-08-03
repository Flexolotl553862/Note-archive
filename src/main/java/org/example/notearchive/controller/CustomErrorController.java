package org.example.notearchive.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              @ModelAttribute("message")
                              String message, Model model
    ) {
        if (message == null || message.isEmpty()) {
            String error = "Unknown error";
            try {
                int code = Integer.parseInt(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE).toString());
                error = code + " " + HttpStatus.resolve(code).getReasonPhrase();
            } catch (NumberFormatException ignored) {
            }

            model.addAttribute("message", error);
        }
        return "not-found";
    }
}
